package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@Path("/fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentAssignmentResource {

  private static final Logger LOGGER = Logger.getLogger(FulfillmentAssignmentResource.class);

  @Inject FulfillmentAssignmentService fulfillmentAssignmentService;

  @Inject FulfillmentAssignmentRepository fulfillmentAssignmentRepository;

  @POST
  @Transactional
  public FulfillmentAssignment assign(FulfillmentAssignmentRequest request) {
    if (request == null) {
      throw new WebApplicationException("Assignment request is required.", 400);
    }

    try {
      return fulfillmentAssignmentService.assign(
          request.storeId, request.productId, request.warehouseBusinessUnitCode);
    } catch (WebApplicationException e) {
      // propagate known web exceptions
      throw e;
    } catch (Exception e) {
      LOGGER.error(
          "Failed to assign fulfillment for storeId="
              + request.storeId
              + ", productId="
              + request.productId
              + ", warehouseBusinessUnitCode="
              + request.warehouseBusinessUnitCode,
          e);
      throw new WebApplicationException("Internal error: " + e.getMessage(), 500);
    }
  }

  @GET
  @Path("/store/{storeId}")
  public List<FulfillmentAssignment> listByStore(@PathParam("storeId") Long storeId) {
    return fulfillmentAssignmentRepository.findByStoreId(storeId);
  }

  @GET
  @Path("/product/{productId}/store/{storeId}")
  public List<FulfillmentAssignment> listByStoreAndProduct(
      @PathParam("productId") Long productId, @PathParam("storeId") Long storeId) {
    return fulfillmentAssignmentRepository.findByStoreAndProduct(storeId, productId);
  }
}
