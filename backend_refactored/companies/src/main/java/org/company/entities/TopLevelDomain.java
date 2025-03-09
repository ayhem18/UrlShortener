package org.company.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)  
public class TopLevelDomain {

    public enum DomainState {
        ACTIVE,
        INACTIVE, // it is possible to temporarily user inactive domains
        DEPRECATED // a deprecated domain cannot no longer be used.
    }

    @Id
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
    private void setId(String id) {
        this.id = id;
    }
    
    private void setDomain(String domain) {
        this.domain = domain;
    }
    
    private void setCompany(Company company) {
        this.company = company;
    }
    
    private void setActivationTime(LocalDateTime activationTime) {
        this.activationTime = activationTime;
    }
    
    private void setDeactivationTime(LocalDateTime deactivationTime) {
        this.deactivationTime = deactivationTime;
    }
    
    private void setDomainState(DomainState domainState) {
        this.domainState = domainState;
    }
}

