/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package agent.gdb.pty.linux;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import agent.gdb.pty.AbstractPtyTest;
import agent.gdb.pty.PtyChild.Echo;
import agent.gdb.pty.PtySession;
import ghidra.dbg.testutil.DummyProc;
import ghidra.framework.OperatingSystem;

public class LinuxPtyTest extends AbstractPtyTest {
	@Before
	public void checkLinux() {
		assumeTrue(OperatingSystem.LINUX == OperatingSystem.CURRENT_OPERATING_SYSTEM);
	}

	@Test
	public void testOpenClosePty() throws IOException {
		LinuxPty pty = LinuxPty.openpty();
		pty.close();
	}

	@Test
	public void testParentToChild() throws IOException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			PrintWriter writer = new PrintWriter(pty.getParent().getOutputStream());
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(pty.getChild().getInputStream()));

			writer.println("Hello, World!");
			writer.flush();
			assertEquals("Hello, World!", reader.readLine());
		}
	}

	@Test
	public void testChildToParent() throws IOException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			PrintWriter writer = new PrintWriter(pty.getChild().getOutputStream());
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(pty.getParent().getInputStream()));

			writer.println("Hello, World!");
			writer.flush();
			assertEquals("Hello, World!", reader.readLine());
		}
	}

	@Test
	public void testSessionBash() throws IOException, InterruptedException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			PtySession bash =
				pty.getChild().session(new String[] { DummyProc.which("bash") }, null);
			pty.getParent().getOutputStream().write("exit\n".getBytes());
			assertEquals(0, bash.waitExited());
		}
	}

	@Test
	public void testForkIntoNonExistent() throws IOException, InterruptedException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			PtySession dies =
				pty.getChild().session(new String[] { "thisHadBetterNotExist" }, null);
			/**
			 * Choice of 127 is based on bash setting "exit code" to 127 for "command not found"
			 */
			assertEquals(127, dies.waitExited());
		}
	}

	@Test
	public void testSessionBashEchoTest() throws IOException, InterruptedException {
		Map<String, String> env = new HashMap<>();
		env.put("PS1", "BASH:");
		env.put("PROMPT_COMMAND", "");
		env.put("TERM", "");
		try (LinuxPty pty = LinuxPty.openpty()) {
			LinuxPtyParent parent = pty.getParent();
			PrintWriter writer = new PrintWriter(parent.getOutputStream());
			BufferedReader reader = loggingReader(parent.getInputStream());
			PtySession bash =
				pty.getChild().session(new String[] { DummyProc.which("bash"), "--norc" }, env);
			runExitCheck(3, bash);

			writer.println("echo test");
			writer.flush();
			String line;
			do {
				line = reader.readLine();
			}
			while (!"test".equals(line));

			writer.println("exit 3");
			writer.flush();

			line = reader.readLine();
			assertTrue("Not 'exit 3' or 'BASH:exit 3': '" + line + "'",
				Set.of("BASH:exit 3", "exit 3").contains(line));

			assertEquals(3, bash.waitExited());
		}
	}

	@Test
	public void testSessionBashInterruptCat() throws IOException, InterruptedException {
		Map<String, String> env = new HashMap<>();
		env.put("PS1", "BASH:");
		env.put("PROMPT_COMMAND", "");
		env.put("TERM", "");
		try (LinuxPty pty = LinuxPty.openpty()) {
			LinuxPtyParent parent = pty.getParent();
			PrintWriter writer = new PrintWriter(parent.getOutputStream());
			BufferedReader reader = loggingReader(parent.getInputStream());
			PtySession bash =
				pty.getChild().session(new String[] { DummyProc.which("bash"), "--norc" }, env);
			runExitCheck(3, bash);

			writer.println("echo test");
			writer.flush();
			String line;
			do {
				line = reader.readLine();
			}
			while (!"test".equals(line));

			writer.println("cat");
			writer.flush();
			line = reader.readLine();
			assertTrue("Not 'cat' or 'BASH:cat': '" + line + "'",
				Set.of("BASH:cat", "cat").contains(line));

			writer.println("Hello, cat!");
			writer.flush();
			assertEquals("Hello, cat!", reader.readLine()); // echo back
			assertEquals("Hello, cat!", reader.readLine()); // cat back

			writer.write(3); // should interrupt
			writer.flush();
			do {
				line = reader.readLine();
			}
			while (!"^C".equals(line));
			writer.println("echo test");
			writer.flush();

			do {
				line = reader.readLine();
			}
			while (!"test".equals(line));

			writer.println("exit 3");
			writer.flush();
			assertTrue(Set.of("BASH:exit 3", "exit 3").contains(reader.readLine()));

			assertEquals(3, bash.waitExited());
		}
	}

	@Test
	public void testLocalEchoOn() throws IOException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			pty.getChild().nullSession();

			PrintWriter writer = new PrintWriter(pty.getParent().getOutputStream());
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(pty.getParent().getInputStream()));

			writer.println("Hello, World!");
			writer.flush();
			assertEquals("Hello, World!", reader.readLine());
		}
	}

	@Test
	public void testLocalEchoOff() throws IOException {
		try (LinuxPty pty = LinuxPty.openpty()) {
			pty.getChild().nullSession(Echo.OFF);

			PrintWriter writerP = new PrintWriter(pty.getParent().getOutputStream());
			PrintWriter writerC = new PrintWriter(pty.getChild().getOutputStream());
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(pty.getParent().getInputStream()));

			writerP.println("Hello, World!");
			writerP.flush();
			writerC.println("Good bye!");
			writerC.flush();

			assertEquals("Good bye!", reader.readLine());
		}
	}
}
