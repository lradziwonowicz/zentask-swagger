package controllers.api;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import models.ApiResponse;
import models.Task;

import org.codehaus.jackson.JsonNode;

import play.mvc.Result;
import play.mvc.Security;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiParamImplicit;

import controllers.Secured;

/**
 * Manage tasks related operations.
 */
@Security.Authenticated(Secured.class)
@Api(value = "/api/tasks", description = "Operations about task")
public class TasksApi extends BaseApiController {

	// -- Tasks

	/**
	 * Update a task
	 */
	@PUT
	@Path("/{id}")
	@ApiOperation(value = "Update a task.")
	@ApiParamImplicit(name = "task", value = "Updated task object", required = true, dataType = "Task", paramType = "body")
	public static Result update(
			@ApiParam(value = "Task id", required = true, allowMultiple = false) @PathParam("id") Long task) {
		if (Secured.isOwnerOf(task)) {
			Task dbTask = Task.find.ref(task);
			Task newTask;
			JsonNode json = request().body().asJson();
			if (json != null) {
				try {
					newTask = (Task) BaseApiController.mapper.readValue(
							json.toString(), Task.class);
					dbTask.title = newTask.title;
					dbTask.done = newTask.done;
					dbTask.folder = newTask.folder;
					dbTask.save();
					return JsonResponse(dbTask);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return JsonResponse(new models.ApiResponse(ApiResponse.ERROR,
				"Invalid parameters"));
	}

	/**
	 * Delete a task
	 */
	@DELETE
	@Path("/{id}")
	@ApiOperation(value = "Delete a task.")
	public static Result delete(
			@ApiParam(value = "Task id", required = true, allowMultiple = false) @PathParam("id") Long task) {
		if (Secured.isOwnerOf(task)) {
			Task.find.ref(task).delete();
			return JsonResponse(new models.ApiResponse(ApiResponse.OK,
					"Task deleted"));
		} else {
			return JsonResponse(new models.ApiResponse(403, "Forbidden"));
		}
	}

	// -- Task folders

	/**
	 * Add a new folder.
	 */
	@POST
	@Path("/folder")
	@ApiOperation(value = "Add a new folder.")
	public static Result addFolder() {
		return JsonResponse(new models.ApiResponse(ApiResponse.OK,
				"Folder created"));
	}

}
