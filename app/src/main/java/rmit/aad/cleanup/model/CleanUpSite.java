package rmit.aad.cleanup.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class CleanUpSite {
    public enum State {Pending, Approved};
    public static ArrayList<String> stateList() {
        ArrayList<String> names = new ArrayList<>();
        for(State value: State.values()) {
            names.add(value.name());
        }
        return names;
    }

    public enum TargetCategory {Community, Organization};
    public static ArrayList<String> targetList() {
        ArrayList<String> names = new ArrayList<>();
        for(TargetCategory value: TargetCategory.values()) {
            names.add(value.name());
        }
        return names;
    }

    public enum LocationType {Beach, Park, Market, Footpath, WalkingStreet, Campus, Building, Others}
    public static ArrayList<String> locationTypeList() {
        ArrayList<String> names = new ArrayList<>();
        for(LocationType value: LocationType.values()) {
            names.add(value.name());
        }
        return names;
    }
    String id;
    String owner;
    List<String> admins;
    int slots;
    int remainingSlots;
    String eventDescription;
    Boolean isPrivate;
    TargetCategory targetCategory;
    State state;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date eventDate;
    String startTime; // time format: "HH:mm"
    String endTime; // time format: "HH:mm"
    Boolean isRecurring;
    RecurringInfo recurringInfo; // if isRecurring = true

    String locationName;
    LocationType locationType;
    String locationAddress;
    String locationDescription;
    double latitude;
    double longitude;
    public CleanUpSite() {};

    public CleanUpSite(String owner, List<String> admins, int slots, int remainingSlots, String eventDescription, Boolean isPrivate, TargetCategory targetCategory, State state, Date eventDate, String startTime, String endTime, Boolean isRecurring, RecurringInfo recurringInfo, String locationName, LocationType locationType, String locationAddress, String locationDescription, double latitude, double longitude) {
        this.owner = owner;
        this.admins = admins;
        this.slots = slots;
        this.remainingSlots = remainingSlots;
        this.eventDescription = eventDescription;
        this.isPrivate = isPrivate;
        this.targetCategory = targetCategory;
        this.state = state;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
        this.recurringInfo = recurringInfo;
        this.locationName = locationName;
        this.locationType = locationType;
        this.locationAddress = locationAddress;
        this.locationDescription = locationDescription;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CleanUpSite(String id, String owner, List<String> admins, int slots, int remainingSlots, String eventDescription, Boolean isPrivate, TargetCategory targetCategory, State state, Date eventDate, String startTime, String endTime, Boolean isRecurring, RecurringInfo recurringInfo, String locationName, LocationType locationType, String locationAddress, String locationDescription, double latitude, double longitude) {
        this.id = id;
        this.owner = owner;
        this.admins = admins;
        this.slots = slots;
        this.remainingSlots = remainingSlots;
        this.eventDescription = eventDescription;
        this.isPrivate = isPrivate;
        this.targetCategory = targetCategory;
        this.state = state;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
        this.recurringInfo = recurringInfo;
        this.locationName = locationName;
        this.locationType = locationType;
        this.locationAddress = locationAddress;
        this.locationDescription = locationDescription;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CleanUpSite(String id, String owner, String[] admins, int slots, int remainingSlots, String eventDescription, Boolean isPrivate, TargetCategory targetCategory, State state, Date eventDate, String startTime, String endTime, Boolean isRecurring, RecurringInfo recurringInfo, String locationName, LocationType locationType, String locationAddress, String locationDescription, double latitude, double longitude) {
        this.id = id;
        this.owner = owner;
        this.admins = Arrays.asList(admins);
        this.slots = slots;
        this.remainingSlots = remainingSlots;
        this.eventDescription = eventDescription;
        this.isPrivate = isPrivate;
        this.targetCategory = targetCategory;
        this.state = state;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
        this.recurringInfo = recurringInfo;
        this.locationName = locationName;
        this.locationType = locationType;
        this.locationAddress = locationAddress;
        this.locationDescription = locationDescription;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public TargetCategory getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(TargetCategory targetCategory) {
        this.targetCategory = targetCategory;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public RecurringInfo getRecurringInfo() {
        return recurringInfo;
    }

    public void setRecurringInfo(RecurringInfo recurringInfo) {
        this.recurringInfo = recurringInfo;
    }

}
