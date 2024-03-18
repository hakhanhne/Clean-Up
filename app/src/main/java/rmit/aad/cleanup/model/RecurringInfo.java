package rmit.aad.cleanup.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;

@IgnoreExtraProperties
public class RecurringInfo {
    public enum Frequency {Weekly, Monthly}
    public static ArrayList<String> frequencyList() {
        ArrayList<String> names = new ArrayList<>();
        for(Frequency value: Frequency.values()) {
            names.add(value.name());
        }
        return names;
    }

    Frequency frequency; //if isRecurring = true
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date endDate;

    public RecurringInfo() {};

    public RecurringInfo(Frequency frequency, Date endDate) {
        this.frequency = frequency;
        this.endDate = endDate;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
