package controllers.api;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.ApiResponse;
import models.Project;
import models.Task;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiParamImplicit;
import com.wordnik.swagger.annotations.ApiParamsImplicit;

import controllers.Secured;

/**
 * Manage projects related operations.
 */
@Security.Authenticated(Secured.class)
@Api(value = "/api/project", description = "Operations about project")
public class ProjectsApi extends BaseApiController {

	/**
	 * Display the dashboard.
	 */
	@GET
	@ApiOperation(value = "List projects", notes = "This can only be done by the logged in user.")
	public static Result index() {
		return JsonResponse(Project.findInvolving(request().username()));
	}

	// -- Projects

	/**
	 * Add a project.
	 */
	@POST
	@Path("/add")
	@ApiOperation(value = "Create project", notes = "This can only be done by the logged in user.")
	@ApiParamsImplicit(@ApiParamImplicit(name = "body", value = "Created project object", required = true, dataType = "Project", paramType = "body"))
	public static Result add() {
		JsonNode json = request().body().asJson();
		Project newProject;
		if (json != null) {
			try {
				newProject = (Project) BaseApiController.mapper.readValue(
						json.toString(), Project.class);
				newProject = Project.create(newProject.name, newProject.folder,
						request().username());
				return JsonResponse(newProject);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return JsonResponse(new models.ApiResponse(400, "Invalid input"));
	}

	/**
	 * Rename a project.
	 */
	@PUT
	@Path("/{id}/rename")
	@ApiOperation(value = "Rename a project", notes = "This can only be done by the project member.")
	@ApiErrors({ @ApiError(code = 400, reason = "Invalid username supplied"),
			@ApiError(code = 404, reason = "User not found") })
	@ApiParamsImplicit({
			@ApiParamImplicit(name = "id", value = "Project id that need to be updated", required = true, dataType = "String", paramType = "path"),
			@ApiParamImplicit(name = "body", value = "Updated project object", required = true, dataType = "Project", paramType = "body") })
	public static Result rename(Long id) {
		if (Secured.isMemberOf(id)) {
			JsonNode json = request().body().asJson();
			Project newProject;
			if (json != null) {
				try {
					newProject = (Project) BaseApiController.mapper.readValue(
							json.toString(), Project.class);
					Project.rename(id, newProject.name);
					return JsonResponse(Project.find.ref(id));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return JsonResponse(new models.ApiResponse(400, "Invalid input"));
		} else {
			return JsonResponse(new models.ApiResponse(400,
					"Invalid username supplied"));
		}
	}

	/**
	 * Delete a project.
	 */
	@DELETE
	@Path("/{id}")
	@ApiOperation(value = "Delete a project.", notes = "This can only be done by the project member.")
	@ApiErrors({ @ApiError(code = 400, reason = "Invalid username supplied"),
			@ApiError(code = 404, reason = "User not found") })
	@ApiParamsImplicit(@ApiParamImplicit(name = "id", value = "The project id that needs to be deleted", required = true, dataType = "Long", paramType = "path"))
	public static Result delete(Long id) {
		if (Secured.isMemberOf(id)) {
			Project.find.ref(id).delete();
			return JsonResponse(new models.ApiResponse(ApiResponse.OK,
					"Project deleted"));
		} else {
			return JsonResponse(new models.ApiResponse(400,
					"Invalid username supplied"));
		}
	}

	// -- Project groups

	/**
	 * Add a new project group.
	 */
	@POST
	@Path("/groups")
	@ApiOperation(value = "Add a new project group.")
	public static Result addGroup() {
		return JsonResponse(new models.ApiResponse(ApiResponse.OK,
				"Group created"));
	}

	/**
	 * Delete a project group.
	 */
	@DELETE
	@Path("/groups")
	@ApiOperation(value = "Delete a project group.")
	public static Result deleteGroup(
			@ApiParam(value = "Group name that needs to be deleted", required = true, allowMultiple = false) @QueryParam("group") String group) {
		Project.deleteInFolder(group);
		return JsonResponse(new models.ApiResponse(ApiResponse.OK,
				"Group deleted"));
	}

	/**
	 * Rename a project group.
	 */
	@GET
	@Path("/groups/{group}")
	@ApiOperation(value = "Rename a project group.")
	public static Result renameGroup(
			@ApiParam(value = "Group name that needs to be changed", required = true, allowMultiple = false) @QueryParam("group") String group,
			@ApiParam(value = "New group name", required = true, allowMultiple = false) @QueryParam("newName") String newName) {
		// response
		ObjectNode result = Json.newObject();
		result.put("name", Project.renameFolder(group, newName));
		return JsonResponse(new models.ApiResponse(ApiResponse.OK,
				"Group renamed"));
	}

	// -- Members

	/**
	 * Add a project member.
	 */
	@POST
	@Path("/{id}/team")
	@ApiOperation(value = "Add a project member.")
	public static Result addUser(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long id,
			@ApiParam(value = "User email address", required = true, allowMultiple = false) @QueryParam("user") String user) {
		if (Secured.isMemberOf(id)) {
			Project.addMember(id, form().bindFromRequest().get("user"));
			return JsonResponse(new models.ApiResponse(ApiResponse.OK,
					"Member added to the project"));
		} else {
			return JsonResponse(new models.ApiResponse(ApiResponse.ERROR,
					"Invalid parameters"));
		}
	}

	/**
	 * Remove a project member.
	 */
	@DELETE
	@Path("/{id}/team")
	@ApiOperation(value = "Remove a project member.")
	public static Result removeUser(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long id,
			@ApiParam(value = "User email address", required = true, allowMultiple = false) @QueryParam("user") String user) {
		if (Secured.isMemberOf(id)) {
			Project.removeMember(id, form().bindFromRequest().get("user"));
			return JsonResponse(new models.ApiResponse(ApiResponse.OK,
					"Member removed from the project"));
		} else {
			return JsonResponse(new models.ApiResponse(ApiResponse.ERROR,
					"Invalid parameters"));
		}
	}

	/**
	 * Display tasks for this project.
	 */
	@GET
	@Path("/{id}/tasks")
	@ApiOperation(value = "Display tasks for this project.")
	public static Result indexTasks(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long id) {
		if (Secured.isMemberOf(id)) {
			return JsonResponse(Project.find.byId(id));
		} else {
			return JsonResponse(new models.ApiResponse(ApiResponse.ERROR,
					"Invalid parameters"));
		}
	}

	/**
	 * Create a task in this project.
	 */
	@POST
	@Path("/{id}/tasks")
	@ApiOperation(value = "Create a task in this project.")
	@ApiErrors({ @ApiError(code = 400, reason = "Invalid data"),
			@ApiError(code = 403, reason = "Forbidden") })
	@ApiParamsImplicit({ @ApiParamImplicit(name = "body", value = "Task object", required = true, dataType = "Task", paramType = "body") })
	public static Result addTask(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long project,
			@ApiParam(value = "Folder name", required = true, allowMultiple = false) @QueryParam("folder") String folder) {
		if (Secured.isMemberOf(project)) {
			JsonNode json = request().body().asJson();
			Task newTask;
			if (json != null) {
				try {
					newTask = (Task) BaseApiController.mapper.readValue(
							json.toString(), Task.class);
					newTask = Task.create(newTask, project, folder);
					return JsonResponse(newTask);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return JsonResponse(new models.ApiResponse(400, "Invalid input"));
	}

	/**
	 * Delete a full tasks folder.
	 */
	@DELETE
	@Path("/{id}/tasks/folder")
	@ApiOperation(value = "Delete a full tasks folder.")
	public static Result deleteFolder(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long project,
			@ApiParam(value = "Folder name", required = true, allowMultiple = false) @QueryParam("folder") String folder) {
		if (Secured.isMemberOf(project)) {
			Task.deleteInFolder(project, folder);
			return JsonResponse(new models.ApiResponse(ApiResponse.OK,
					"Folder deleted"));
		} else {
			return JsonResponse(new models.ApiResponse(403, "Forbidden"));
		}
	}

	/**
	 * Rename a tasks folder.
	 */
	@PUT
	@Path("/{id}/tasks/folder")
	@ApiOperation(value = "Rename a tasks folder.")
	public static Result renameFolder(
			@ApiParam(value = "Project id", required = true, allowMultiple = false) @PathParam("id") Long project,
			@ApiParam(value = "Folder name", required = true, allowMultiple = false) @QueryParam("folder") String folder,
			@ApiParam(value = "New folder name", required = true, allowMultiple = false) @QueryParam("name") String name) {
		if (Secured.isMemberOf(project)) {
			return JsonResponse(Task.renameFolder(project, folder, name));
		} else {
			return JsonResponse(new models.ApiResponse(400, "Invalid input"));
		}
	}

}
