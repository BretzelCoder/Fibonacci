using System.Numerics;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Fibonacci.Api.Infrastructure;

/// <summary>
/// Serializes BigInteger as a JSON string to avoid JavaScript's 53-bit safe integer limit.
/// Register in Program.cs via JsonSerializerOptions.Converters.
/// </summary>
public sealed class BigIntegerJsonConverter : JsonConverter<BigInteger>
{
    public override BigInteger Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        => BigInteger.Parse(reader.GetString()!);

    public override void Write(Utf8JsonWriter writer, BigInteger value, JsonSerializerOptions options)
        => writer.WriteStringValue(value.ToString());
}
