package eu.hansolo.nameservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/names")
public class Api {
    protected final Logger     logger   = Logger.getLogger(Api.class.getName());
    private   final List<Name> allNames;

    /**
     * Example call: http://localhost:8080/names/?gender=girl&amount=5
     */
    public Api() {
        final long start = System.nanoTime();
        this.allNames = Helper.loadNames();
        logger.info(String.format("258000 names loaded in (%d) ms", ((System.nanoTime() - start) / 1_000_000)));
    }


    @RequestMapping(path="/", method = RequestMethod.GET)
    public String randomNames(@RequestParam("gender") String gender, @RequestParam("amount") Integer amount) {
        logger.info(String.format("Api.randomNames(%d)", amount));

        final long       start   = System.nanoTime();
        final Gender     _gender = (null == gender || gender.isEmpty()) ? Constants.RND.nextBoolean() ? Gender.FEMALE : Gender.MALE : Gender.fromText(gender);
        final int        _amount = (null == amount || amount < 1 || amount > 100) ? 5 : amount;
        final List<Name> names   = Helper.getRandomNames(this.allNames, _amount, _gender);

        // Sort names
        names.sort(Comparator.comparing(Name::getName));

        // Create Json output
        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("{")
                  .append("  \"names\":[");
        names.forEach(name -> msgBuilder.append("{")
                                        .append("\"").append("name").append("\":\"").append(name.getName()).append("\",")
                                        .append("\"").append("gender").append("\":\"").append(name.getGender().name().toLowerCase()).append("\"")
                                        .append("},"));
        msgBuilder.setLength(msgBuilder.length() - 2);
        msgBuilder.append("\n],")
                  .append("\"response_time\":\"")
                  .append((System.nanoTime() - start) / 1_000_000)
                  .append(" ms\"")
                  .append("}");

        return msgBuilder.toString();
    }
}
