using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http.Filters;
using VISIDA_API.Controllers;

namespace VISIDA_API.Providers
{
    public class TimingActionFilter : ActionFilterAttribute
    {
        public override void OnActionExecuted(HttpActionExecutedContext actionExecutedContext)
        {
            base.OnActionExecuted(actionExecutedContext);

            var baseController = actionExecutedContext.ActionContext.ControllerContext.Controller;
            var a = baseController.GetType().IsSubclassOf(typeof(AuthController));
            if (a)
            {
                var controller = baseController as AuthController;
                var time = controller.timer.ElapsedMilliseconds;

                HttpContext.Current.Response.Headers.Add("processTime", time.ToString());
                string test = HttpContext.Current.Response.Headers.Get("Access-Control-Expose-Headers");
                if (test != null)
                    HttpContext.Current.Response.Headers["Access-Control-Expose-Headers"] = test + ",processTime";
                else
                    HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "processTime");
            }
        }
    }
}