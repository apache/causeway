/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2005  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.distribution.broadcast;


import java.io.IOException;
import java.io.OutputStream;


class CounterOutputStream extends OutputStream {
    private int size = 0;
    private java.io.OutputStream out;

    public CounterOutputStream(OutputStream out) {
        super();
        this.out = out;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public int getSize() {
        return size;
    }

    public void write(int b) throws IOException {
        out.write(b);
        size++;
    }

    public void write(byte b[]) throws IOException {
        out.write(b);
        size += b.length;
    }

    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        size += len;
    }
}
