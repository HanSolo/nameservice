package eu.hansolo.nameservice.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class Name {
    private String name;
    private Gender gender;


    public Name(final String name, final Gender gender) {
        this.name   = name;
        this.gender = gender;
    }
    public Name(final String nameJson) {
        if (null == nameJson || nameJson.isEmpty()) { throw new IllegalArgumentException("name json cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(nameJson, JsonObject.class);

        final String name   = json.get("name").getAsString();
        final Gender gender = Gender.fromText(json.get("gender").getAsString());

        this.name   = name;
        this.gender = gender;
    }


    public final String getName() { return this.name; }

    public final Gender getGender() { return this.gender; }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"").append("name").append("\":\"").append(this.name).append("\",")
                                  .append("\"").append("gender").append("\":\"").append(this.gender.name().toLowerCase()).append("\"")
                                  .append("}")
                                  .toString().replaceAll("\\\\", "");
    }
}
