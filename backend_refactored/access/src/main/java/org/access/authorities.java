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


class CanDeleteAccess implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_DELETE_ACCESS";
    private static volatile CanDeleteAccess instance;
    
    public CanDeleteAccess() {
        if (instance == null) {
            synchronized (CanDeleteAccess.class) {
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


class CanCancelSubscription implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_CANCEL_SUBSCRIPTION";
    private static volatile CanCancelSubscription instance;
    
    public CanCancelSubscription() {
        if (instance == null) {
            synchronized (CanCancelSubscription.class) {
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


class CanGenerateTokens implements GrantedAuthority {
    private static final String AUTHORITY = "CAN_GENERATE_TOKENS";
    private static volatile CanGenerateTokens instance;
    
    public CanGenerateTokens() {
        if (instance == null) {
            synchronized (CanGenerateTokens.class) {
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







