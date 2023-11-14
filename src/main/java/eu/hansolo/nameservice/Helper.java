package eu.hansolo.nameservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class Helper {

    public static final List<Name> getRandomNames(final List<Name> allNames, final int amount, final Gender gender) {
        if (amount > allNames.size()) { throw new IllegalArgumentException("Given amount cannot be greater than number of all names"); }
        if (amount < 1) { throw new IllegalArgumentException("amount should at least be 1"); }

        final List<Name> namesByGivenGender = allNames.stream()
                                                      .filter(name -> Gender.ALL == gender ? name.getGender() != null : gender == name.getGender())
                                                      .collect(Collectors.toList());
        if (namesByGivenGender.isEmpty()) { return new ArrayList<>(); }

        final Random    rnd         = new Random();
        final Set<Name> randomNames = new HashSet<>();
        while(randomNames.size() < amount) {
            randomNames.add(namesByGivenGender.get(rnd.nextInt(namesByGivenGender.size() - 1)));
        }
        return new ArrayList<>(randomNames);
    }

    public static List<Name> loadNames() {
        final List<Name> namesFound = new ArrayList<>();
        try(JsonReader jsonReader = new JsonReader(new InputStreamReader(Helper.class.getResourceAsStream(Constants.DATA_FILENAME), StandardCharsets.UTF_8))) {
            final Gson gson = new GsonBuilder().create();
            jsonReader.beginArray();
            while (jsonReader.hasNext()){
                NameDto nameDto = gson.fromJson(jsonReader, NameDto.class);
                namesFound.add(new Name(nameDto.toString()));
            }
            jsonReader.endArray();
        }  catch (IOException e) {
            return namesFound;
        }
        return namesFound;
    }
}
