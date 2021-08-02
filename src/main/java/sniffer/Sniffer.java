
package sniffer;

import com.sun.jna.Platform;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PcapHandle.PcapDirection;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;

public class Sniffer {
    private ArrayList<PcapNetworkInterface> interfaceArrayList;
    private PcapHandle handle;
    private PcapDirection direction;
    private String ip;
    private static final String IP_PATTERN = "((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private final Pattern pattern = Pattern.compile("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    public Sniffer() {
    }

    private boolean ipValidate(String ip) {
        return this.pattern.matcher(ip).matches();
    }

    public void allInterface() {
        try {
            this.interfaceArrayList = (ArrayList)Pcaps.findAllDevs();
            Iterator var1 = this.interfaceArrayList.iterator();

            while(var1.hasNext()) {
                PcapNetworkInterface s = (PcapNetworkInterface)var1.next();
                System.out.println("[" + this.interfaceArrayList.indexOf(s) + "]: " + s.getName());
            }
        } catch (PcapNativeException var3) {
            var3.printStackTrace();
        } catch (ClassCastException var4) {
            System.out.println("No NIF was found. Please check that install pcap.");
            System.exit(0);
        }

    }

    public void start() throws PcapNativeException, IOException {
        int snapshotLength = 65536;
        int readTimeout = 50;
        ServerSocket serverSocket = new ServerSocket(6379);
        Socket socket = serverSocket.accept();
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        this.allInterface();
        System.out.println("\nPlease enter number of interface for start sniffing: ");
        int num = -1;

        for(Scanner scanner = new Scanner(System.in); num < 0 || num >= this.interfaceArrayList.size(); num = this.selectNumInterface(scanner)) {
        }

        PcapNetworkInterface networkInterface = (PcapNetworkInterface)this.interfaceArrayList.get(num);
        System.out.println("You choose interface " + networkInterface.getName());
        this.handle = networkInterface.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
        System.out.println("\nPlease type ip or press enter. Input format: src/dst <ip>. P.s. Sorry choice direction not working on Windows, it`s pcap4j feature. :)");
        this.addFilter();
        PacketListener listener = (packet) -> {
            out.println(packet.length());
        };

        try {
            int maxPackets = 2147483647;
            this.handle.loop(maxPackets, listener);
        } catch (NotOpenException | InterruptedException var11) {
            var11.printStackTrace();
        }

    }

    public void addFilter() {
        Scanner scanner = new Scanner(System.in);
        String filter = "";

        while(true) {
            filter = scanner.nextLine();
            if ("".equals(filter)) {
                System.out.println("You didn`t add filter.");
                break;
            }

            if (this.parseFilter(filter.split(" "))) {
                System.out.println("You add filter.");
                break;
            }
        }

        try {
            if (this.ip != null) {
                this.handle.setFilter("ip", BpfCompileMode.OPTIMIZE, (Inet4Address)InetAddress.getByName(this.ip));
            }

            if (this.direction != null) {
                if (Platform.isWindows()) {
                    System.out.println("Sorry, direction choice is not working on Windows. This step will pass.");
                    return;
                }

                this.handle.setDirection(this.direction);
            }
        } catch (PcapNativeException var4) {
            var4.printStackTrace();
        } catch (NotOpenException var5) {
            var5.printStackTrace();
        } catch (UnknownHostException var6) {
            var6.printStackTrace();
        }

    }

    private boolean parseFilter(String[] filter) {
        String[] var2 = filter;
        int var3 = filter.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String f = var2[var4];
            if ("src".equals(f)) {
                if (this.direction == null) {
                    this.direction = PcapDirection.IN;
                }
            } else if ("dst".equals(f)) {
                if (this.direction == null) {
                    this.direction = PcapDirection.OUT;
                }
            } else {
                if (!this.ipValidate(f)) {
                    System.out.println("In filter typo. Please use this template: dst/src <ip_address>. You can enter also ip");
                    return false;
                }

                this.ip = f;
            }
        }

        return true;
    }

    public int selectNumInterface(Scanner scanner) {
        int num = -1;

        try {
            while(true) {
                num = Integer.valueOf(scanner.next());
                if (num >= 0 && num < this.interfaceArrayList.size()) {
                    return num;
                }

                System.out.println("Please select interface from the above list");
            }
        } catch (NumberFormatException var4) {
            System.out.println("Please enter the interface number");
            return num;
        }
    }
}
