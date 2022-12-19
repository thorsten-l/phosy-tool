import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import static l9g.app.phosy.ucware.UcwareAttributeType.*;

// input args : Entry entry        (LDAP entry)
              : LdapUtil ldapUtil

String username = ldapUtil.value(LDAP_UID);
String firstname = ldapUtil.value(LDAP_GIVENNAME);
String lastname = ldapUtil.value(LDAP_SN);
String phoneNumber = ldapUtil.value(LDAP_TELEPHONENUMBER); // extension
String email = ldapUtil.value(LDAP_MAIL);
String locality = ldapUtil.value(LDAP_LOCALITY);
String externalId = DN.normalize(entry.getDN());
ArrayList<String> groupNames = new ArrayList<>();
ArrayList<String> licenses = new ArrayList<>();

String url = "https://www.ostfalia.de/";
String language = "de";
String authBackend = "ostfalialdap";
boolean privacy = true;

licenses.add("5"); // bundle License

switch (locality.toLowerCase())
{
  case "wolfenbüttel":
    groupNames.add("Users-WF");
    break;
  case "wolfsburg":
    groupNames.add("Users-WOB");
    break;
  case "salzgitter":
    groupNames.add("Users-SZ");
    break;
  case "suderburg":
    groupNames.add("Users-Sud");
    break;
}

boolean doNotImport = false;
boolean doNotUpdate = false;

username;  // <= Return value

/* JDK >=12
groupNames.add (
  switch (locality.toLowerCase())
  {
    case "wolfenbüttel" -> "Users-WF";
    case "wolfsburg" -> "Users-WOB";
    case "salzgitter" -> "Users-SZ";
    case "suderburg" -> "Users-Sud";
  }
);
*/
