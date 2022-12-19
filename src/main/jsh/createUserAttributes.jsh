
import java.util.ArrayList;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import l9g.app.phosy.ucware.UcwareAttributeType;

// input args : Entry entry        (LDAP entry)
//            : LdapUtil ldapUtil

String username = ldapUtil.value(UcwareAttributeType.LDAP_UID);
String firstname = ldapUtil.value(UcwareAttributeType.LDAP_GIVENNAME);
String lastname = ldapUtil.value(UcwareAttributeType.LDAP_SN);
String phoneNumber = ldapUtil.value(UcwareAttributeType.LDAP_TELEPHONENUMBER);
String email = ldapUtil.value(UcwareAttributeType.LDAP_MAIL);
String locality = ldapUtil.value(UcwareAttributeType.LDAP_LOCALITY);
String externalId = DN.normalize(entry.getDN());
ArrayList<String> groupNames = new ArrayList<>();
ArrayList<String> slotTypes = new ArrayList<>();
ArrayList<Integer> licenses = new ArrayList<>();

String url = "https://www.ostfalia.de/";
String language = "de";
String authBackend = "ostfalia";
boolean privacy = true;

licenses.add(5); // bundle license

slotTypes.add("mac");       // table phone
slotTypes.add("webrtc");    // software desktop client
slotTypes.add("sip-ua");    // SIP phone
slotTypes.add("mobile");    // mobile phone
slotTypes.add("ipei");      // DECT phone

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

boolean doNotCreate = false;
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
