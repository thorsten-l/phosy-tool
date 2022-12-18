import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import static l9g.app.phosy.ucware.UcwareAttributeType.*;

// input args : Entry entry        (LDAP entry)
              : LdapUtil ldapUtil

boolean doNotImport = false;
boolean doNotUpdate = false;

String username = ldapUtil.value(LDAP_UID);
String firstname = ldapUtil.value(LDAP_GIVENNAME);
String lastname = ldapUtil.value(LDAP_SN);
String phoneNumber = ldapUtil.value(LDAP_TELEPHONENUMBER);
String email = ldapUtil.value(LDAP_MAIL);
String locality = ldapUtil.value(LDAP_LOCALITY);

String url = "https://www.ostfalia.de/";
String language = "de";
String authBackend = "ostfalia";
boolean privacy = true;

username;  // <= Return value
