package bbc.forge.dsp.jaxrs;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class JaxRsStats {

	private AtomicInteger resourceNotAvailable = new AtomicInteger();
	private AtomicInteger invalidDescriptor = new AtomicInteger();
	private AtomicInteger repositoryFailure = new AtomicInteger();
	private AtomicInteger invalidContent = new AtomicInteger();
	private AtomicInteger invalidApiConfig = new AtomicInteger();
	private AtomicInteger resourceAlreadyExists = new AtomicInteger();
	private AtomicInteger resourceNotFound = new AtomicInteger();
	private AtomicInteger resourceModified = new AtomicInteger();
	private AtomicLong totalResponseTime = new AtomicLong();
	private AtomicInteger totalResponseCount = new AtomicInteger();
	private AtomicInteger flagpoleReadError = new AtomicInteger();
    private AtomicInteger total200ResponseCounter = new AtomicInteger();
    private AtomicInteger total201ResponseCounter = new AtomicInteger();
    private AtomicInteger public200ResponseCounter = new AtomicInteger();
    private AtomicInteger private200ResponseCounter = new AtomicInteger();
    private AtomicInteger stats200ResponseCounter = new AtomicInteger();

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Public_200_response_counter")
    public AtomicInteger getPublic200ResponseCounter() {
        return public200ResponseCounter;
    }

    public void incrementPublic200ResponseCounter() {
        public200ResponseCounter.incrementAndGet();
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Private_200_response_counter")
    public AtomicInteger getPrivate200ResponseCounter() {
        return private200ResponseCounter;
    }

    public void incrementPrivate200ResponseCounter() {
        private200ResponseCounter.incrementAndGet();
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Stats_endpoints_200_response_counter")
    public AtomicInteger getStats200ResponseCounter() {
        return stats200ResponseCounter;
    }

    public void incrementStats200ResponseCounter() {
        stats200ResponseCounter.incrementAndGet();
    }


	public void incrementResourceNotAvailable() {
		resourceNotAvailable.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_Resources_that_are_not_available")
	public AtomicInteger getResourceNotAvailable() {
		return resourceNotAvailable;
	}

	public void incrementInvalidDescriptor() {
		invalidDescriptor.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_Invalid_Descriptors")
	public AtomicInteger getInvalidDescriptor() {
		return invalidDescriptor;
	}

	public void incrementRepositoryFailure() {
		repositoryFailure.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_Repository_Failures")
	public AtomicInteger getRepositoryFailure() {
		return repositoryFailure;
	}

	public void incrementInvalidContent() {
		invalidContent.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_Invalid_Content_Failures")
	public AtomicInteger getInvalidContent() {
		return invalidContent;
	}

	public void incrementInvalidApiConfig() {
		invalidApiConfig.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_Invalid_API_Config_Failures")
	public AtomicInteger getInvalidApiConfig() {
		return invalidApiConfig;
	}

	public void incrementResourceAlreadyExists() {
		resourceAlreadyExists.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_already_existant_resources")
	public AtomicInteger getResourceAlreadyExists() {
		return resourceAlreadyExists;
	}

	public void incrementResourceNotFound() {
		resourceNotFound.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_not_found_resources")
	public AtomicInteger getResourceNotFound() {
		return resourceNotFound;
	}

	public void incrementResourceModified() {
		resourceModified.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_unexpectedly_modified_resources")
	public AtomicInteger getResourceModified() {
		return resourceModified;
	}

	@ManagedAttribute(description = "valueType=GAUGE|kpiName=JaxRsStats_Average_response_time_in_Ms")
	public long getAverageResponseTime() {
		if(totalResponseCount.get()==0){
			return 0;
		}
		return totalResponseTime.get() / totalResponseCount.get();
	}

	public void incrementResponseTime(long duration) {
		totalResponseCount.incrementAndGet();
		totalResponseTime.addAndGet(duration);
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Errors_when_reading_flagpole_headers")
	public AtomicInteger getFlagpoleReadError() {
		return flagpoleReadError;
	}

	public void incrementFlagpoleReadError() {
		flagpoleReadError.incrementAndGet();
	}

    public void incrementTotal200ResponseCounter() {
        total200ResponseCounter.incrementAndGet();
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Total_Count_of_200_Responses")
    public AtomicInteger getTotal200ResponseCounter() {
        return total200ResponseCounter;
    }

    public void incrementTotal201ResponseCounter() {
        total201ResponseCounter.incrementAndGet();
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Total_Count_of_201_Responses")
    public AtomicInteger getTotal201ResponseCounter() {
        return total201ResponseCounter;
    }
}
