package sniffer;

public class PacketSniff {
    private long size;

    public PacketSniff(String size) {
        this.size = Long.parseLong(size);
    }

    public long getSize() {
        return this.size;
    }
}
