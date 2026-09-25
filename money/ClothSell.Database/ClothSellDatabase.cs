using SqlSugar;

namespace ClothSell.Database;

public sealed class ClothSellDatabase
{
    private ClothSellDatabase(ISqlSugarClient client)
    {
        Client = client;
    }

    public ISqlSugarClient Client { get; }

    public static ClothSellDatabase Create(string? connectionString, string? password)
    {
        if (string.IsNullOrWhiteSpace(connectionString))
        {
            connectionString = "Host=127.0.0.1;Port=5432;Database=cloth_sell;Username=postgres;Password="
                + (password ?? "");
        }
        var client = new SqlSugarClient(new ConnectionConfig
        {
            ConnectionString = connectionString,
            DbType = DbType.PostgreSQL,
            IsAutoCloseConnection = true
        });
        return new ClothSellDatabase(client);
    }
}

[SugarTable("mall_cart")]
public class MallCart
{
    [SugarColumn(IsPrimaryKey = true)]
    public long Id { get; set; }

    [SugarColumn(ColumnName = "user_id")]
    public long UserId { get; set; }

    [SugarColumn(ColumnName = "sku_id")]
    public long SkuId { get; set; }

    public int Qty { get; set; }

    public int Deleted { get; set; }
}

[SugarTable("mall_sku")]
public class MallSku
{
    [SugarColumn(IsPrimaryKey = true)]
    public long Id { get; set; }

    [SugarColumn(ColumnName = "product_id")]
    public long ProductId { get; set; }

    public decimal Price { get; set; }

    public int Deleted { get; set; }
}

[SugarTable("mall_order")]
public class MallOrder
{
    [SugarColumn(IsPrimaryKey = true)]
    public long Id { get; set; }

    public decimal Freight { get; set; }

    [SugarColumn(ColumnName = "total_amount")]
    public decimal TotalAmount { get; set; }

    public int Deleted { get; set; }
}

[SugarTable("mall_order_line")]
public class MallOrderLine
{
    [SugarColumn(IsPrimaryKey = true)]
    public long Id { get; set; }

    [SugarColumn(ColumnName = "order_id")]
    public long OrderId { get; set; }

    [SugarColumn(ColumnName = "sku_id")]
    public long SkuId { get; set; }

    public decimal Price { get; set; }

    public int Qty { get; set; }

    public int Deleted { get; set; }
}
