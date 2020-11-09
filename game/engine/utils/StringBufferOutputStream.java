package game.engine.utils;

import java.io.IOException;
import java.io.OutputStream;

public final class StringBufferOutputStream extends OutputStream {
   private StringBuffer buffer;

   public StringBufferOutputStream(StringBuffer sb) {
      this.buffer = sb;
   }

   public void write(int b) throws IOException {
      this.buffer.append((char)b);
   }
}
