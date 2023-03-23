import org.json.JSONObject;

public interface FlightService {
    JSONObject execute(JSONObject params) throws Exception;
}
