/*
 * MIT License
 *
 * Copyright (c) 2019 dsafa22, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fang.myapplication.dnssd;

import android.content.Context;

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRecordRegistrar;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.DomainListener;
import com.apple.dnssd.QueryListener;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.RegisterRecordListener;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

public class DNSSDBindable extends DNSSD {

    private final Context context;

    public DNSSDBindable(Context context) {
        super();
        this.context = context.getApplicationContext();
    }

    @Override
    protected DNSSDService _makeBrowser(int flags, int ifIndex, String regType, String domain, BrowseListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected DNSSDService _resolve(int flags, int ifIndex, String serviceName, String regType, String domain, ResolveListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected DNSSDRegistration _register(int flags, int ifIndex, String serviceName, String regType, String domain, String host, int port, TXTRecord txtRecord, RegisterListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected DNSSDRecordRegistrar _createRecordRegistrar(RegisterRecordListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected DNSSDService _queryRecord(int flags, int ifIndex, String serviceName, int rrtype, int rrclass, QueryListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected DNSSDService _enumerateDomains(int flags, int ifIndex, DomainListener listener) throws DNSSDException {
        return null;
    }

    @Override
    protected String _constructFullName(String serviceName, String regType, String domain) throws DNSSDException {
        return null;
    }

    @Override
    protected void _reconfirmRecord(int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata) {

    }

    @Override
    protected String _getNameForIfIndex(int ifIndex) {
        return null;
    }

    @Override
    protected int _getIfIndexForName(String ifName) {
        return 0;
    }
/*
    @Override
    public void onServiceStarting() {
        super.onServiceStarting();
        context.getSystemService(Context.NSD_SERVICE);
    }

    @Override
    public void onServiceStopped() {
        super.onServiceStopped();
        // Not used in bindable version
    }

    *//** Return the canonical name of a particular interface index.<P>
     @param	ifIndex
     A valid interface index. Must not be ALL_INTERFACES.
     <P>
     @return		The name of the interface, which should match java.net.NetworkInterface.getName().

     @throws SecurityException If a security manager is present and denies <tt>RuntimePermission("getDNSSDInstance")</tt>.
     @see    RuntimePermission
     *//*
    public String getNameForIfIndex(int ifIndex) {
        return InternalDNSSD.getNameForIfIndex(ifIndex);
    }*/
}
