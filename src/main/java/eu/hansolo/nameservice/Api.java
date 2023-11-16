package eu.hansolo.nameservice;

import org.crac.Context;
import org.crac.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/names")
public class Api implements Resource {
    protected final Logger     logger   = Logger.getLogger(Api.class.getName());
    private   final List<Name> allNames = new ArrayList<>();

    /**
     * Example call    : http://localhost:8080/names/?gender=boy&amount=5
     * Example response: { "names":[{"name":"Deandre","gender":"male"},{"name":"Jesse","gender":"male"},{"name":"Kermit","gender":"male"},{"name":"Salvador","gender":"male"},{"name":"Santos","gender":"male" ],"response_time":"16 ms"}
     */
    public Api() {

    }


    @RequestMapping(path="/", method = RequestMethod.GET)
    public String randomNames(@RequestParam("gender") String gender, @RequestParam("amount") Integer amount) {
        this.logger.info(String.format("Api.randomNames(%d)", amount));
        final long       start   = System.nanoTime();

        // Load names if allNames.isEmpty()
        if (this.allNames.isEmpty()) { this.allNames.addAll(Helper.loadNames()); }

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


    // #################### CRaC specific methods #############################
    @Override public void beforeCheckpoint(final Context<? extends Resource> context) throws Exception {
        this.logger.info("CRaC beforeCheckpoint called");
        if (this.allNames.isEmpty()) {
            this.logger.info("Load all names before the checkpoint");
            this.allNames.addAll(Helper.loadNames());
        }
    }

    @Override public void afterRestore(final Context<? extends Resource> context) throws Exception {
        this.logger.info("CRaC afterRestore called");
    }
}
