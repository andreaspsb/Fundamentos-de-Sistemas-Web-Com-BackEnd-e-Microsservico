using Microsoft.AspNetCore.Http;
using PetshopApi.Security;
using System.Threading.Tasks;

namespace PetshopApi.Middleware
{
    public class JwtMiddleware
    {
        private readonly RequestDelegate _next;

        public JwtMiddleware(RequestDelegate next)
        {
            _next = next;
        }

        public async Task Invoke(HttpContext context, JwtService jwtService)
        {
            var token = context.Request.Headers["Authorization"].FirstOrDefault()?.Split(" ").Last();

            if (token != null)
            {
                var principal = jwtService.ValidateToken(token);
                if (principal != null)
                {
                    context.User = principal;
                    
                    // Adicionar informações úteis ao HttpContext
                    context.Items["Username"] = principal.Identity?.Name;
                    context.Items["Role"] = principal.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;
                    context.Items["ClienteId"] = principal.FindFirst("clienteId")?.Value;
                }
            }

            await _next(context);
        }
    }
}
