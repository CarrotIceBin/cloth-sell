using ClothSell.Database;

namespace ClothSell.Money;

public static class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);
        builder.WebHost.UseUrls("http://127.0.0.1:5088");
        ClothSellDatabase database = ClothSellDatabase.Create(
            builder.Configuration["Mysql"],
            builder.Configuration["MysqlPassword"] ?? Environment.GetEnvironmentVariable("POSTGRES_PASSWORD") ?? Environment.GetEnvironmentVariable("MYSQL_PASSWORD"));
        var repository = new MoneyRepository(database.Client);
        var app = builder.Build();

        app.MapPost("/cart/{userId:long}/amounts", (long userId) =>
        {
            CartAmountResult result = repository.CartAmounts(userId);
            return Results.Ok(new
            {
                items = result.Items.Select(row => new
                {
                    cartId = row.CartId,
                    price = MoneyRepository.Text(row.Price),
                    amount = MoneyRepository.Text(row.Amount)
                }),
                freight = "0.00",
                payable = MoneyRepository.Text(result.Payable)
            });
        });

        app.MapPost("/products/min-prices", (IdList body) =>
        {
            List<ProductMinRow> rows = repository.MinPrices(body.ProductIds ?? []);
            return Results.Ok(new
            {
                items = rows.Select(row => new
                {
                    productId = row.ProductId,
                    minPrice = MoneyRepository.Text(row.MinPrice)
                })
            });
        });

        app.MapPost("/orders/{orderId:long}/amounts", (long orderId) =>
        {
            decimal? payable = repository.FillOrder(orderId);
            if (payable == null)
            {
                return Results.NotFound();
            }
            return Results.Ok(new { freight = "0.00", payable = MoneyRepository.Text(payable.Value) });
        });

        app.Run();
    }
}

public sealed record IdList(long[]? ProductIds);
