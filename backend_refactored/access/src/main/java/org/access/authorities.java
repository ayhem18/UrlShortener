package org.access;

import org.springframework.security.core.GrantedAuthority;

class CanUpdateDomainName implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_UPDATE_DOMAIN_NAME";
    private static volatile CanUpdateDomainName instance;
    
    public CanUpdateDomainName() {
        if (instance == null) {
            synchronized (CanUpdateDomainName.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanDeleteCompany implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_DELETE_COMPANY";
    private static volatile CanDeleteCompany instance;
    
    public CanDeleteCompany() {
        if (instance == null) {
            synchronized (CanDeleteCompany.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanWorkWithSubscription implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_WORK_WITH_SUBSCRIPTION";
    private static volatile CanWorkWithSubscription instance;
    
    public CanWorkWithSubscription() {  
        if (instance == null) {
            synchronized (CanWorkWithSubscription.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanViewSubscription implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_VIEW_SUBSCRIPTION";
    private static volatile CanViewSubscription instance;
    
    public CanViewSubscription() {
        if (instance == null) {
            synchronized (CanViewSubscription.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanViewCompanyDetails implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_VIEW_COMPANY_DETAILS";
    private static volatile CanViewCompanyDetails instance;
    
    public CanViewCompanyDetails() {
        if (instance == null) {
            synchronized (CanViewCompanyDetails.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}




class CanWorkWithTokens implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_WORK_WITH_TOKENS";
    private static volatile CanWorkWithTokens instance;
    
    public CanWorkWithTokens() {
        if (instance == null) {
            synchronized (CanWorkWithTokens.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanUseShortUrl implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_USE_SHORT_URL";
    private static volatile CanUseShortUrl instance;
    
    public CanUseShortUrl() {
        if (instance == null) {
            synchronized (CanUseShortUrl.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanEncodeUrl implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_ENCODE_URL";
    private static volatile CanEncodeUrl instance;
    
    public CanEncodeUrl() {
        if (instance == null) { 
            synchronized (CanEncodeUrl.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}


class CanViewHistory implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_VIEW_HISTORY";
    private static volatile CanViewHistory instance;
    
    public CanViewHistory() {
        if (instance == null) { 
            synchronized (CanViewHistory.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }   
    
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}   







