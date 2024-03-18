package rmit.aad.cleanup.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SiteEnrolment {
    String siteid;
    String userid;

    public SiteEnrolment() {}

    public SiteEnrolment(String siteid, String userid) {
        this.siteid = siteid;
        this.userid = userid;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

}
