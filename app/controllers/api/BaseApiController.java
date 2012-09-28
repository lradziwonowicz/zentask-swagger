package controllers.api;

import java.io.StringWriter;

import com.wordnik.swagger.core.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;

public class BaseApiController extends Controller {
	static JavaRestResourceUtil ru = new JavaRestResourceUtil();

	protected static ObjectMapper mapper = JsonUtil.getJsonMapper();

	public static Result JsonResponse(Object obj) {
		return JsonResponse(obj, 200);
	}

	public static Result JsonResponse(Object obj, int code) {
		StringWriter w = new StringWriter();
		try {
			mapper.writeValue(w, obj);
		} catch (Exception e) {
			// TODO: handle proper return code
			e.printStackTrace();
		}

		response().setContentType("application/json");
		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(w.toString());
	}
}