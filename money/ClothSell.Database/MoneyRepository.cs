using System.Globalization;
using SqlSugar;

namespace ClothSell.Database;

public sealed class MoneyRepository
{
    private readonly ISqlSugarClient db;

    public MoneyRepository(ISqlSugarClient db)
    {
        this.db = db;
    }

    public CartAmountResult CartAmounts(long userId)
    {
        List<CartAmountRow> items = db.Queryable<MallCart, MallSku>((cart, sku) => cart.SkuId == sku.Id)
            .Where((cart, sku) => cart.UserId == userId && cart.Deleted == 0 && sku.Deleted == 0)
            .OrderBy((cart, sku) => cart.Id)
            .Select((cart, sku) => new CartAmountRow
            {
                CartId = cart.Id,
                Price = sku.Price,
                Amount = SqlFunc.Round(sku.Price * cart.Qty, 2)
            })
            .ToList();
        decimal payable = db.Queryable<MallCart, MallSku>((cart, sku) => cart.SkuId == sku.Id)
            .Where((cart, sku) => cart.UserId == userId && cart.Deleted == 0 && sku.Deleted == 0)
            .Sum((cart, sku) => SqlFunc.Round(sku.Price * cart.Qty, 2));
        return new CartAmountResult { Items = items, Payable = payable };
    }

    public List<ProductMinRow> MinPrices(IReadOnlyList<long> productIds)
    {
        if (productIds.Count == 0)
        {
            return [];
        }
        return db.Queryable<MallSku>()
            .Where(sku => sku.Deleted == 0 && productIds.Contains(sku.ProductId))
            .GroupBy(sku => sku.ProductId)
            .Select(sku => new ProductMinRow
            {
                ProductId = sku.ProductId,
                MinPrice = SqlFunc.AggregateMin(sku.Price)
            })
            .ToList();
    }

    public decimal? FillOrder(long orderId)
    {
        try
        {
            db.Ado.BeginTran();
            db.Updateable<MallOrderLine>()
                .InnerJoin<MallSku>((line, sku) => line.SkuId == sku.Id && sku.Deleted == 0)
                .SetColumns((line, sku) => new MallOrderLine { Price = sku.Price })
                .Where((line, sku) => line.OrderId == orderId && line.Deleted == 0)
                .ExecuteCommand();
            decimal payable = db.Queryable<MallOrderLine>()
                .Where(line => line.OrderId == orderId && line.Deleted == 0)
                .Sum(line => SqlFunc.Round(line.Price * line.Qty, 2));
            int updated = db.Updateable<MallOrder>()
                .SetColumns(order => new MallOrder { Freight = 0m, TotalAmount = payable })
                .Where(order => order.Id == orderId && order.Deleted == 0)
                .ExecuteCommand();
            if (updated != 1)
            {
                db.Ado.RollbackTran();
                return null;
            }
            db.Ado.CommitTran();
            return payable;
        }
        catch
        {
            db.Ado.RollbackTran();
            throw;
        }
    }

    public static string Text(decimal value) => value.ToString("0.00", CultureInfo.InvariantCulture);
}

public sealed class CartAmountResult
{
    public List<CartAmountRow> Items { get; set; } = [];
    public decimal Payable { get; set; }
}

public sealed class CartAmountRow
{
    public long CartId { get; set; }
    public decimal Price { get; set; }
    public decimal Amount { get; set; }
}

public sealed class ProductMinRow
{
    public long ProductId { get; set; }
    public decimal MinPrice { get; set; }
}
