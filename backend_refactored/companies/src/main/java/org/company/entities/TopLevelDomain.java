package org.company.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)  
public class TopLevelDomain {
    public enum DomainState {
        ACTIVE,
        INACTIVE, // it is possible to temporarily user inactive domains
        DEPRECATED // a deprecated domain can no longer be used.
    }

    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    private String domain;

    @DocumentReference
    private Company company;
    
    private LocalDateTime activationTime;

    private LocalDateTime deactivationTime;
    
    private DomainState domainState;

    public TopLevelDomain(String id, String domain, Company company) {
        this.id = id;
        this.domain = domain;

        this.company = company;
        this.activationTime = LocalDateTime.now();
        this.domainState = DomainState.ACTIVE;
        this.deactivationTime = null;
    }
    
    // Private no-argument constructor for Jackson serialization
    @SuppressWarnings("unused")
    private TopLevelDomain() {
    }
    
    // Method to deactivate the domain
    public void deactivate() {
        if (this.domainState != DomainState.ACTIVE) {
            throw new IllegalStateException("Domain is not active and cannot be deactivated");
        }
        this.domainState = DomainState.INACTIVE;
        this.deactivationTime = LocalDateTime.now();
    }

    public void deprecate() {
        if (this.domainState == DomainState.DEPRECATED) {
            throw new IllegalStateException("Domain is already deprecated");
        }

        // if the domain is active, set the deactivation time as well
        if (this.domainState == DomainState.ACTIVE) {
            this.deactivationTime = LocalDateTime.now();
        }

        this.domainState = DomainState.DEPRECATED;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getDomain() {
        return domain;
    }

    public Company getCompany() {
        return company;
    }
    
    public LocalDateTime getActivationTime() {
        return activationTime;
    }
    
    public LocalDateTime getDeactivationTime() {
        return deactivationTime;
    }
    
    public DomainState getDomainState() {
        return domainState;
    }
    
    // Private setters
    @SuppressWarnings("unused")
    private void setId(String id) {
        this.id = id;
    }
    
    @SuppressWarnings("unused")
    private void setDomain(String domain) {
        this.domain = domain;
    }


    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }
    
    @SuppressWarnings("unused")
    private void setActivationTime(LocalDateTime activationTime) {
        this.activationTime = activationTime;
    }
    
    @SuppressWarnings("unused")
    private void setDeactivationTime(LocalDateTime deactivationTime) {
        this.deactivationTime = deactivationTime;
    }
    
    @SuppressWarnings("unused")
    private void setDomainState(DomainState domainState) {
        this.domainState = domainState;
    }
}

