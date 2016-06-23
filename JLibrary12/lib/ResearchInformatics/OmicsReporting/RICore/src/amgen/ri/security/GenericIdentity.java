package amgen.ri.security;

import amgen.ri.ldap.AmgenEnterpriseEntry;

public class GenericIdentity extends AbstractIdentity implements IdentityIF {
	
	public GenericIdentity(String username) {
		super(username);
	}

}
