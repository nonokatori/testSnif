
package json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

public class JSONWorker {
    private String ip_db;
    private String port_db;
    private String log_db;
    private String pass_db;
    private String ip_kafka;
    private String port_kafka;
    private StringBuilder builder = new StringBuilder("");

    public JSONWorker() {
        while(true) {
            boolean b = this.readJSON();
            if (b) {
                System.out.println("Read configuration from file.");
                this.jsonParse();
                return;
            }

            System.out.println("File was not found. Create and init new json.");
        }
    }

    private void jsonParse() {
        JSONObject obj = new JSONObject(this.builder.toString());
        this.ip_db = obj.getString("ip_db");
        this.ip_kafka = obj.getString("ip_kafka");
        this.port_kafka = obj.getString("port_kafka");
        this.port_db = obj.getString("port_db");
        this.pass_db = obj.getString("password");
        this.log_db = obj.getString("login");
    }

    private boolean readJSON() {
        String path = "src/main/resources/property.json";
        String strJson = "";

        try {
            FileReader r = new FileReader(path);

            try {
                BufferedReader reader = new BufferedReader(r);

                try {
                    for(strJson = reader.readLine(); strJson != null; strJson = reader.readLine()) {
                        this.builder.append(strJson);
                    }
                } catch (Throwable var9) {
                    try {
                        reader.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                reader.close();
            } catch (Throwable var10) {
                try {
                    r.close();
                } catch (Throwable var7) {
                    var10.addSuppressed(var7);
                }

                throw var10;
            }

            r.close();
        } catch (FileNotFoundException var11) {
            this.createJSON(path);
            return false;
        } catch (IOException var12) {
            var12.printStackTrace();
        }

        return true;
    }

    private void createJSON(String path) {
        JSONObject jo = new JSONObject();
        jo.put("ip_db", "127.0.0.1");
        jo.put("port_db", "3306");
        jo.put("login", "user");
        jo.put("password", "user");
        jo.put("ip_kafka", "127.0.0.1");
        jo.put("port_kafka", "4306");
        File f = new File(path);

        try {
            f.createNewFile();
            FileWriter writer = new FileWriter(f);
            writer.write(jo.toString());
            writer.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public String getPort_db() {
        return this.port_db;
    }

    public String getLog_db() {
        return this.log_db;
    }

    public String getPass_db() {
        return this.pass_db;
    }

    public String getIp_kafka() {
        return this.ip_kafka;
    }

    public String getPort_kafka() {
        return this.port_kafka;
    }

    public String getIp_db() {
        return this.ip_db;
    }
}
