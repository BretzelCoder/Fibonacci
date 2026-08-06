using System.Text.Json.Serialization;
using Fibonacci.Api.Algorithms;
using Fibonacci.Api.Infrastructure;
using Fibonacci.Api.Services;
using Microsoft.AspNetCore.Diagnostics;
using Scalar.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// ── Controllers + JSON ────────────────────────────────────────────────────────
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new BigIntegerJsonConverter());
        options.JsonSerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.Never;
        options.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
    });

builder.Services.AddOpenApi();

// ── In-process cache (bounded) ────────────────────────────────────────────────
// SizeLimit is the maximum number of cache entries (one per distinct n value).
builder.Services.AddMemoryCache(options => options.SizeLimit = 10_000);

// ── Fibonacci algorithms ──────────────────────────────────────────────────────
// Each concrete type is registered as BOTH its own type AND as IFibonacciAlgorithm.
// Registration order controls the display order in API responses.
// .NET DI resolves IEnumerable<IFibonacciAlgorithm> by collecting ALL IFibonacciAlgorithm
// registrations in order — registering as IEnumerable<T> directly does NOT work for this.
builder.Services.AddSingleton<NaiveRecursiveAlgorithm>();
builder.Services.AddSingleton<IFibonacciAlgorithm>(sp => sp.GetRequiredService<NaiveRecursiveAlgorithm>());

builder.Services.AddSingleton<MemoizedAlgorithm>();
builder.Services.AddSingleton<IFibonacciAlgorithm>(sp => sp.GetRequiredService<MemoizedAlgorithm>());

builder.Services.AddSingleton<IterativeAlgorithm>();
builder.Services.AddSingleton<IFibonacciAlgorithm>(sp => sp.GetRequiredService<IterativeAlgorithm>());

builder.Services.AddSingleton<FastDoublingAlgorithm>();
builder.Services.AddSingleton<IFibonacciAlgorithm>(sp => sp.GetRequiredService<FastDoublingAlgorithm>());

builder.Services.AddScoped<IFibonacciService, FibonacciService>();

// ── CORS — permit the Vite dev server origin ──────────────────────────────────
builder.Services.AddCors(options =>
    options.AddPolicy("AllowVueDev", policy =>
        policy.WithOrigins("http://localhost:5173")
              .AllowAnyMethod()
              .AllowAnyHeader()));

// ── Build ─────────────────────────────────────────────────────────────────────
var app = builder.Build();

// Global exception handler — always returns a JSON error body so the frontend
// can display a meaningful message instead of a raw 500.
app.UseExceptionHandler(errorApp => errorApp.Run(async context =>
{
    context.Response.StatusCode = 500;
    context.Response.ContentType = "application/json";
    var ex = context.Features.Get<IExceptionHandlerFeature>()?.Error;
    await context.Response.WriteAsJsonAsync(new
    {
        message = ex?.Message ?? "An unexpected error occurred.",
        type = ex?.GetType().Name,
    });
}));

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.MapScalarApiReference();
}

app.UseCors("AllowVueDev");
app.UseAuthorization();
app.MapControllers();

app.Run();

// Expose the implicit Program class for integration test project access.
public partial class Program { }
