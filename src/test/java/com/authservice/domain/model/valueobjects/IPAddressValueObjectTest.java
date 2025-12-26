package com.authservice.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IPAddressValueObjectTest {

    // --------- Valid IPv4 Addresses ---------
    @Test
    void constructor_shouldAcceptValidIPv4() {
        String[] validIPv4 = {
                "192.168.0.1",
                "0.0.0.0",
                "255.255.255.255",
                "127.0.0.1"
        };

        for (String ip : validIPv4) {
            IPAddressValueObject ipObj = new IPAddressValueObject(ip);
            assertEquals(ip, ipObj.getValue());
        }
    }

    // --------- Valid IPv6 Addresses ---------
    @Test
    void constructor_shouldAcceptValidIPv6() {
        String[] validIPv6 = {
                "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                "fe80:0000:0000:0000:0202:b3ff:fe1e:8329",
                "1234:5678:9abc:def0:1234:5678:9abc:def0"
        };

        for (String ip : validIPv6) {
            IPAddressValueObject ipObj = new IPAddressValueObject(ip);
            assertEquals(ip, ipObj.getValue());
        }
    }

    // --------- Invalid IP Addresses ---------
    @Test
    void constructor_shouldThrowExceptionForInvalidIP() {
        String[] invalidIPs = {
                "256.100.100.100", // invalid IPv4
                "192.168.0", // incomplete IPv4
                "12345::6789::abcd", // invalid IPv6
                "abcd", // random string
                "192.168.0.1.1", // extra segment
                "" // empty string
        };

        for (String ip : invalidIPs) {
            assertThrows(IllegalArgumentException.class,
                    () -> new IPAddressValueObject(ip), "IP should be invalid: " + ip);
        }
    }

    // --------- Null Input ---------
    @Test
    void constructor_shouldThrowExceptionForNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new IPAddressValueObject(null));
    }

    // --------- Trimming Behavior ---------
    @Test
    void constructor_shouldTrimInput() {
        String ipWithSpaces = "   192.168.1.1   ";
        IPAddressValueObject ipObj = new IPAddressValueObject(ipWithSpaces);
        assertEquals("192.168.1.1", ipObj.getValue());
    }
}
