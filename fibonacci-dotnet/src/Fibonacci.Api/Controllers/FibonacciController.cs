using Fibonacci.Api.Models;
using Fibonacci.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace Fibonacci.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public sealed class FibonacciController : ControllerBase
{
    private readonly IFibonacciService _service;

    public FibonacciController(IFibonacciService service) => _service = service;

    /// <summary>Computes all Fibonacci algorithm variants for a given n.</summary>
    /// <param name="request">n (0–5000) and whether to include the naive recursive variant.</param>
    /// <returns>Per-algorithm timing, cache statistics, and an integrity check.</returns>
    [HttpPost("compute")]
    [ProducesResponseType<ComputeResponse>(StatusCodes.Status200OK)]
    [ProducesResponseType<ValidationProblemDetails>(StatusCodes.Status400BadRequest)]
    public ActionResult<ComputeResponse> Compute([FromBody] ComputeRequest request)
    {
        try
        {
            return Ok(_service.ComputeAll(request.N, request.IncludeNaive));
        }
        catch (ArgumentOutOfRangeException ex)
        {
            return BadRequest(new { error = ex.ParamName, message = ex.Message });
        }
    }
}
