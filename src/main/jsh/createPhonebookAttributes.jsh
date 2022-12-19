
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import l9g.app.phosy.LdapHandler;
import l9g.app.phosy.PhonebookHandler;
import static l9g.app.phosy.ucware.phonebook.model.UcwareAttributeType.*;

// input args : Entry entry         (LDAP entry)

String prefix = "";
String suffix = "";
String syncId = LdapHandler.buildSyncId(entry);

String company = ldapUtil.value(UCW_COMPANY);
String department = ldapUtil.value(UCW_DEPARTMENT);
String hyperlink = ldapUtil.value(UCW_HYPERLINK);
String position = ldapUtil.value(UCW_POSITION);
String phoneNumber = ldapUtil.value(UCW_PHONENUMBER_HIGH_PRIORITY);
String academicTitle = ldapUtil.value(LDAP_ACADEMIC_TITLE);
String locality = ldapUtil.value(LDAP_LOCALITY);

department = department.toUpperCase();

if ("RZ".equals(department))
{
  department = "RECHENZENTRUM";
}

if (academicTitle.trim().length() > 0)
{
  suffix = academicTitle + " (" + department + ")";
}
else
{
  suffix = "(" + department + ")";
}

if (syncId.contains(",o=ostfalia.de,"))
{
  company = "Ostfalia";
  hyperlink = "https://www.ostfalia.de";
}
else if (syncId.contains(",o=hbk-bs.de,"))
{
  company = "HBK-BS";
  hyperlink = "https://www.hbk-bs.de";
}
else if (syncId.contains(",o=3landesmuseen.de,"))
{
  company = "3Landesmuseen";
  hyperlink = "https://www.3landesmuseen.de";
}

phoneNumber = phoneNumber.trim();
phoneNumber = phoneNumber.replaceAll("\\s+", "");
phoneNumber = phoneNumber.replaceFirst("\\+49", "0");
phoneNumber = phoneNumber.replaceFirst("^00049", "0");
phoneNumber = phoneNumber.replaceFirst("^0049", "0");
phoneNumber = phoneNumber.replaceFirst("^00", "0");
phoneNumber = phoneNumber.replace("(0)", "");

locality = locality.toLowerCase();

if (!phoneNumber.startsWith("0"))
{
  String prefix = "";
  if (syncId.contains(",o=ostfalia.de,"))
  {
    prefix = "05331939";
    switch (locality)
    {
      case "wolfsburg":
        prefix = "053618922";
        break;
      case "salzgitter":
        prefix = "05341875";
        break;
      case "suderburg":
        prefix = "05826988";
        break;
    }
  }
  else if (syncId.contains(",o=hbk-bs.de,"))
  {
    prefix = "0531391" + phoneNumber;
  }
  else if (syncId.contains(",o=3landesmuseen.de,"))
  {
    prefix = "05311225" + phoneNumber;
  }
  phoneNumber = prefix + phoneNumber;
}

phoneNumber;  // <= Return value
