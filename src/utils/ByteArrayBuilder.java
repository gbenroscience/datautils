package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteArrayBuilder implements Cloneable {

    private class Buffer {

        private final List<byte[]> store = Collections.synchronizedList(new ArrayList<>());
        private final AtomicInteger size = new AtomicInteger();

        private synchronized void reset() {
            size.set(0);
            store.clear();
        }
    }

    private final Buffer buffer;
    private byte[] realStore = {};

    public ByteArrayBuilder() {
        buffer = new Buffer();
    }

    public synchronized void sync() {
        reconcile();
    }

    public synchronized void clear() {
        realStore = new byte[]{};
        buffer.reset();
    }
    
    /**
     *
     * @param lng A long number
     * @return its byte array representation
     */
    private static byte[] toBytes(long lng) {
        byte[] b = new byte[]{
            (byte) lng,
            (byte) (lng >> 8),
            (byte) (lng >> 16),
            (byte) (lng >> 24),
            (byte) (lng >> 32),
            (byte) (lng >> 40),
            (byte) (lng >> 48),
            (byte) (lng >> 56)};
        return b;
    }

    /**
     *
     * @param value An integer number
     * @return its byte array representation
     */
    private static byte[] toBytes(int value) {
        return new byte[]{
            (byte) ((value >> 24) & 0xff),
            (byte) ((value >> 16) & 0xff),
            (byte) ((value >> 8) & 0xff),
            (byte) ((value >> 0) & 0xff),};
    }

    /**
     * Converts a shorts array to an array of bytes
     *
     * @param shorts The shorts array
     * @return a byte array
     */
    private static byte[] toBytes(short[] shorts) {
        int shortArrsize = shorts.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (shorts[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (shorts[i] >> 8);
            shorts[i] = 0;
        }
        return bytes;

    }
    /**
     *
     * @return a copy of this {@link ByteArrayBuilder}. The clone is not linked
     * to the original at all, except for the fact that it holds exactly same
     * data at the instant of cloning; alone. Subsequent modifications to the
     * original {@link ByteArrayBuilder} have no effect on the clone.
     */
    @Override
    public synchronized ByteArrayBuilder clone() {
        reconcile();
        ByteArrayBuilder b = new ByteArrayBuilder();
        b.realStore = new byte[this.realStore.length];
        System.arraycopy(realStore, 0, b.realStore, 0, realStore.length);
        return b;
    }

    /**
     * Merges the items stored in the buffer with the items in the real store.
     */
    private void reconcile() {

        if (buffer == null || buffer.store.isEmpty()) {
            return;
        }

        synchronized (buffer) {

            if (realStore == null || realStore.length == 0) {
                realStore = new byte[buffer.size.get()];
                buffer.size.set(0);
                int ind = 0;

                for (byte[] elem : buffer.store) {
                    System.arraycopy(elem, 0, realStore, ind, elem.length);
                    ind += elem.length;
                }
            } else {
                byte[] temp = new byte[realStore.length + buffer.size.get()];
                buffer.size.set(0);
                int ind = 0;
                System.arraycopy(realStore, 0, temp, 0, realStore.length);
                ind += realStore.length;

                for (byte[] elem : buffer.store) {
                    System.arraycopy(elem, 0, temp, ind, elem.length);
                    ind += elem.length;
                }

                this.realStore = temp;
            }

            buffer.reset();
        }
    }

    /**
     * Always slower than {@link ByteArrayBuilder#append(byte[])}. Use in a loop
     * only when you can't use {@link ByteArrayBuilder#append(byte[])} to
     * accomplish your logic
     *
     * @param data The data to prepend
     * @return the original object with the fresh data prepended to it.
     */
    public ByteArrayBuilder prepend(byte[] data) {
        insert(0, data);
        return this;
    }

    /**
     *
     * @param number Converts the long value to a byte array and appends it
     * @return the original object with the long's bytes appended to it.
     */
    public synchronized ByteArrayBuilder append(long number) {
        return append(toBytes(number), false);
    }

    /**
     *
     * @param number Converts the int value to a byte array and appends it
     * @return the original object with the int's bytes appended to it.
     */
    public synchronized ByteArrayBuilder append(int number) {
        return append(toBytes(number), false);
    }

    /**
     *
     * @param number Converts the short value to a byte array and appends it
     * @return the original object with the short's bytes appended to it.
     */
    public synchronized ByteArrayBuilder append(short number) {
        return append(toBytes(number), false);
    }

    public synchronized ByteArrayBuilder append(byte data) {
        return append(new byte[]{data}, false);
    }

    /**
     * Appends the byte array to this builder at very high speed. When done with
     * calls to this method, always call {@link ByteArrayBuilder#sync()} to
     *
     * @param data A byte array to append to the {@link ByteArrayBuilder}
     * @return the instance of this builder object to facilitate chaining calls
     */
    public synchronized ByteArrayBuilder append(byte[] data) {
        return append(data.clone(), false);
    }

    /**
     * Appends the portion of this byte array starting from
     * <code>fromIndex</code> up to and including <code>toIndex</code> to this
     * builder at very high speed. When done with calls to this method, always
     * call {@link ByteArrayBuilder#sync()} to
     *
     * @param data A byte array to append to the {@link ByteArrayBuilder}
     * @param fromIndex index to start copying from
     * @param toIndex The final index to copy
     * @return the instance of this builder object to facilitate chaining calls
     */
    public synchronized ByteArrayBuilder append(byte[] data, int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IllegalArgumentException("fromIndex must be >= 0");
        }
        if (fromIndex >= data.length) {
            throw new IllegalArgumentException("fromIndex must be < " + data.length);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        if (toIndex < 0) {
            throw new IllegalArgumentException("toIndex must be >= 0");
        }
        if (toIndex >= data.length) {
            throw new IllegalArgumentException("toIndex must be < " + data.length);
        }

        byte[] bits = new byte[toIndex - fromIndex + 1];
        System.arraycopy(data, fromIndex, bits, 0, bits.length);
        return append(bits, false);
    }

    /**
     * @param data A byte array to append to the {@link ByteArrayBuilder}
     * @param last If true, this is the last item in a sequence of appends. When
     * last is true, the call reconciles all prior append operations and creates
     * the builder structure.
     * @return the instance of this builder object to facilitate chaining calls
     */
    public synchronized ByteArrayBuilder append(byte[] data, boolean last) {

        buffer.store.add(data);
        buffer.size.addAndGet(data.length);

        if (last) {
            reconcile();
        }

        return this;
    }

    /**
     * Insert may or may not be as fast as an append depending on the index of
     * insertion Example //index of insertion = 2; //[3,1,6,5,7,8,4]//original
     * //[0,1,2,3,4,5,6]
     * <p>
     * //[9,2]-->data
     * <p>
     * //[3,1,9,2,6,5,7,8,4]
     *
     * Inserts a byte array at the given index of the {@link ByteArrayBuilder}
     *
     * @param index The index in the byte array at which to insert the byte
     * array supplied
     * @param data A byte array to insert at the given index in the
     * {@link ByteArrayBuilder}
     * @return
     */
    public synchronized ByteArrayBuilder insert(int index, byte[] data) {

        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index: " + index + " > " + realStore.length);
        }

        if (index == 0 && realStore.length == 0 || index == realStore.length) {
            append(data.clone());
            return this;
        }

        if (index > realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index: " + index + " > " + realStore.length);
        }

        reconcile();

        byte[] temp = new byte[realStore.length + data.length];

        synchronized (realStore) {

            int ind = 0;
            System.arraycopy(realStore, 0, temp, ind, index);
            ind += index;

            System.arraycopy(data, 0, temp, ind, data.length);
            ind += data.length;

            System.arraycopy(realStore, index, temp, ind, realStore.length - index);

            this.realStore = temp;

            return this;

        }

    }

    /**
     *
     * @param index The index whose byte we wish to return
     * @return the byte at that index
     */
    public byte get(int index) {
        reconcile();

        if (realStore.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot access index (" + index + ") in empty builder");
        }
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index cannot be negative.");
        }
        if (index >= realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + index + ") >= Builder Size(" + realStore.length + ")");
        }

        return realStore[index];

    }

    /**
     *
     * @param index The index whose byte we wish to update
     * @param number The data to set at the specified index.
     */
    public synchronized void set(int index, byte number) {
        reconcile();

        if (realStore.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot access index (" + index + ") in empty builder. Append some data first!");
        }
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index cannot be negative.");
        }
        if (index >= realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + index + ") >= Builder Size(" + realStore.length + ")");
        }

        realStore[index] = number;

    }

    /**
     * Updates the data over a range of the {@link ByteArrayBuilder}
     *
     * @param startIndex The index where we wish to set some data in
     * {@link ByteArrayBuilder##realStore}
     * @param data The data to set at the specified index.
     * @return the byte at that index
     */
    public synchronized ByteArrayBuilder set(int startIndex, byte[] data) {
        reconcile();

        if (realStore.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot access index (" + startIndex + ") in empty builder. Append some data first!");
        }
        if (startIndex < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index cannot be negative.");
        }
        if (startIndex >= realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + startIndex + ") >= Builder Size(" + realStore.length + ")");
        }
        if (startIndex + data.length > realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + startIndex + ") + Input Length >= Builder Size(" + realStore.length + ") Space not enough!");
        }

        System.arraycopy(data, 0, realStore, startIndex, data.length);

        //[4,9,1,2,6,3,7,0,8,5]
        //              [2,1,3,9]
        //[0,1,2,3,4,5,6,7,8,9]
        return this;

    }

    public int length() {
        reconcile();
        return realStore.length;
    }

    /**
     *
     * @param startIndex The index from which we wish to copy some data in
     * {@link ByteArrayBuilder##realStore}
     * @param numberOfItems The number of items to copy
     */
    public synchronized byte[] get(int startIndex, int numberOfItems) {
        reconcile();

        if (realStore.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot access index (" + startIndex + ") in empty builder. Append some data first!");
        }
        if (startIndex < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index cannot be negative.");
        }
        if (startIndex >= realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + startIndex + ") >= Builder Size(" + realStore.length + ")");
        }
        if (startIndex + numberOfItems > realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + startIndex + ") + Input Length >= Builder Size(" + realStore.length + ") Space not enough!");
        }

        byte[] data = new byte[numberOfItems];
        System.arraycopy(realStore, startIndex, data, 0, numberOfItems);
        //[4,9,1,2,6,3,7,0,8,5]
        //              [2,1,3,9]
        //[0,1,2,3,4,5,6,7,8,9]

        return data;

    }

    /**
     *
     * @param start The start index
     * @param numberOfItems The number of items to remove
     * @return the original {@link ByteArrayBuilder}, now modified
     */
    public synchronized ByteArrayBuilder remove(int start, int numberOfItems) {

        reconcile();
        int end = start + numberOfItems - 1;

        if (realStore.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Cannot access index (" + start + ") in empty builder. Append some data first!");
        }
        if (start < 0) {
            throw new ArrayIndexOutOfBoundsException("Input Index cannot be negative.");
        }
        if (start >= realStore.length) {
            throw new ArrayIndexOutOfBoundsException("Input Index (" + start + ") >= Builder Size(" + realStore.length + ")");
        }
        if (start >= end) {
            throw new ArrayIndexOutOfBoundsException("Start Index (" + start + ") >= End Index(" + end + ") is just plain wrong");
        }

        if (end > realStore.length) {
            throw new ArrayIndexOutOfBoundsException("End Index (" + end + ") > Builder Size(" + realStore.length + ") is an out-of-bounds indexing error!");
        }

        byte[] data = new byte[realStore.length - numberOfItems];

        int ind = 0;

        //[4,5,2,3,1,8,6,9]
        //[0,1,2,3,4,5,6,7] from 2 , 3 elems...end = start+num-1 = 2 + 3 - 1 = 4
        //[4,5,8,6]
        System.arraycopy(realStore, 0, data, ind, start);

        ind += start;

        System.arraycopy(realStore, end + 1, data, ind, realStore.length - end - 1);

        this.realStore = data;
        return this;
    }

    /**
     *
     * @return the builder data as a byte array
     */
    public synchronized byte[] getBytes() {
        reconcile();
        return realStore;
    }

    @Override
    public String toString() {
        reconcile();
        synchronized (realStore) {

            StringBuilder b = new StringBuilder("[");
            for (byte e : realStore) {
                b.append(e).append(" , ");
            }
            if (realStore.length == 0) {
                return "[]; item-count = 0";
            } else {
                return b.substring(0, b.length() - 3).concat("]").concat("; item-count = " + length());
            }
        }
    }

    public void log() {
        System.out.println(toString());
    }

    private static void print(byte[] data) {

        StringBuilder b = new StringBuilder("[");

        for (byte e : data) {
            b.append(e).append(" , ");
        }
        String out = b.substring(0, b.length() - 3).concat("]");

        System.out.println(out);
    }

}
