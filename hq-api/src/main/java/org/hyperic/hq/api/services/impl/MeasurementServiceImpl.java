package org.hyperic.hq.api.services.impl;

import javax.ws.rs.core.Response;

import org.hibernate.ObjectNotFoundException;
import org.hyperic.hq.api.model.measurements.MeasurementRequest;
import org.hyperic.hq.api.model.measurements.MeasurementResponse;
import org.hyperic.hq.api.model.measurements.ResourceMeasurementBatchResponse;
import org.hyperic.hq.api.model.measurements.ResourceMeasurementRequests;
import org.hyperic.hq.api.services.MeasurementService;
import org.hyperic.hq.api.transfer.MeasurementTransfer;
import org.hyperic.hq.api.transfer.mapping.ExceptionToErrorCodeMapper;
import org.hyperic.hq.auth.shared.SessionNotFoundException;
import org.hyperic.hq.auth.shared.SessionTimeoutException;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.common.TimeframeBoundriesException;
import org.hyperic.hq.measurement.server.session.Measurement;
import org.hyperic.hq.measurement.server.session.MeasurementTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.text.ParseException;

public class MeasurementServiceImpl extends RestApiService implements MeasurementService {
    @Autowired
    private MeasurementTransfer measurementTransfer;
    @Autowired
    private ExceptionToErrorCodeMapper errorHandler ; 
    
	public MeasurementResponse getMetrics(final MeasurementRequest measurementRequest,
			final String rscId, final String begin, final String end)
			        throws Throwable {
	    try {
	        try {
//	        String begin = begin_.toString();
	        ApiMessageContext apiMessageContext = newApiMessageContext();
	        return measurementTransfer.getMetrics(apiMessageContext, measurementRequest, rscId, begin, end);
	        
	        } catch (Throwable t) {
	            errorHandler.log(t);
	            throw t;
	        }
        } catch (UnsupportedOperationException e) {
            throw errorHandler.newWebApplicationException(Response.Status.BAD_REQUEST, ExceptionToErrorCodeMapper.ErrorCode.BAD_MEASUREMENT_REQ, e.getMessage());
        } catch (ParseException e) {
            throw errorHandler.newWebApplicationException(Response.Status.BAD_REQUEST, ExceptionToErrorCodeMapper.ErrorCode.WRONG_DATE_FORMAT, e.getMessage());
        } catch (ObjectNotFoundException e) {
            String missingObj = e.getEntityName();
            if (MeasurementTemplate.class.getName().equals(missingObj)) {
                throw errorHandler.newWebApplicationException(Response.Status.NOT_FOUND, ExceptionToErrorCodeMapper.ErrorCode.TEMPLATE_NOT_FOUND, e.getIdentifier());
            }
            if (Measurement.class.getName().equals(missingObj)) {
                throw errorHandler.newWebApplicationException(Response.Status.NOT_FOUND, ExceptionToErrorCodeMapper.ErrorCode.MEASUREMENT_NOT_FOUND, e.getIdentifier());
            }
            throw errorHandler.newWebApplicationException(Response.Status.NOT_FOUND, ExceptionToErrorCodeMapper.ErrorCode.RESOURCE_NOT_FOUND_BY_ID, e.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw errorHandler.newWebApplicationException(Response.Status.BAD_REQUEST, ExceptionToErrorCodeMapper.ErrorCode.WRONG_DATE_VALUES, e.getMessage());
        }
    }

    public ResourceMeasurementBatchResponse getAggregatedMetricData(ResourceMeasurementRequests hqMsmtReqs, String begin, String end)
            throws ParseException, PermissionException, SessionNotFoundException, SessionTimeoutException {
        try {
            ApiMessageContext apiMessageContext = newApiMessageContext();
            return measurementTransfer.getAggregatedMetricData(apiMessageContext, hqMsmtReqs, begin, end);
        } catch (ParseException e) {
            throw errorHandler.newWebApplicationException(Response.Status.BAD_REQUEST, ExceptionToErrorCodeMapper.ErrorCode.WRONG_DATE_FORMAT, "cannot parse the begin/end time frame arguments\n",e.getMessage());
        } catch (TimeframeBoundriesException e) {
            throw errorHandler.newWebApplicationException(Response.Status.BAD_REQUEST, ExceptionToErrorCodeMapper.ErrorCode.WRONG_DATE_VALUES, e.getMessage());
        }
    }
}