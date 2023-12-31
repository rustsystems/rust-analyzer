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
package ghidra.app.merge.listing;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Test;

import docking.AbstractErrDialog;
import generic.theme.GThemeDefaults.Colors.Palette;
import ghidra.program.database.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.util.*;
import ghidra.util.SaveableColor;
import ghidra.util.exception.DuplicateNameException;

/**
 * Test the merge of the versioned program's listing.
 */
public class UserDefinedPropertyMergeManagerTest extends AbstractListingMergeManagerTest {

	// ********************
	// *** DiffTestPgm1 ***
	// ********************
	// ** SPACE property
	// 0x10018ae: space=1.
	// 0x10018ba: space=1.
	// 0x10018ff: space=1.
	// 0x100248c: space=1.
	// ** testColor property
	// 0x100248c: testColor=CYAN.
	// 0x10039dd: testColor=BLACK.
	// 0x10039f8: testColor=BLACK.
	// 0x10039fe: testColor=RED.

	// ********************
	// *** DiffTestPgm2 ***
	// ********************
	// ** SPACE property
	// 0x10018ba: space=1.
	// 0x10018ce: space=2.
	// 0x10018ff: space=2.
	// ** testColor property
	// 0x100248c: testColor=WHITE.
	// 0x10039f1: testColor=BLACK.
	// 0x10039f8: testColor=BLACK.
	// 0x10039fe: testColor=GREEN.

	public UserDefinedPropertyMergeManagerTest() {
		super();
	}

	@Test
	public void testAddVoidProperty() throws Exception {
		mtf.initialize("DiffTestPgm1", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					VoidPropertyMap vpm = pmm.createVoidPropertyMap("testVoidProp");
					vpm.add(addr(program, "0x1002472"));
					vpm.add(addr(program, "0x100248c"));
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					VoidPropertyMap vpm = pmm.createVoidPropertyMap("testVoidProp");
					vpm.add(addr(program, "0x1002481"));
					vpm.add(addr(program, "0x100248c"));
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}
		});

		executeMerge(ASK_USER);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		VoidPropertyMap vpm = pmm.getVoidPropertyMap("testVoidProp");
		Address address = resultProgram.getMinAddress();
		address = vpm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002472"), address);
		address = vpm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002481"), address);
		address = vpm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		address = vpm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testAddSpaceProperty() throws Exception {
		// ********************
		// *** DiffTestPgm2 ***
		// ********************
		// ** SPACE property
		// 0x10018ba: space=1.
		// 0x10018ce: space=2.
		// 0x10018ff: space=2.
		// ** testColor property
		// 0x100248c: testColor=WHITE.
		// 0x10039f1: testColor=BLACK.
		// 0x10039f8: testColor=BLACK.
		// 0x10039fe: testColor=GREEN.
		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x1002472"), 1);
				pm.add(addr(program, "0x1002488"), 3);
				pm.add(addr(program, "0x100248a"), 3);
				pm.add(addr(program, "0x100248c"), 3);
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x1002481"), 2);
				pm.add(addr(program, "0x1002488"), 2);
				pm.add(addr(program, "0x100248a"), 2);
				pm.add(addr(program, "0x100248c"), 2);
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x1002488"), "Space", KEEP_LATEST);
		chooseUserDefined(addr("0x100248a"), "Space", KEEP_ORIGINAL);
		chooseUserDefined(addr("0x100248c"), "Space", KEEP_MY);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002428"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002472"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002481"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002488"), address);
		assertEquals(3, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testAddObjectProperty() throws Exception {
		// ********************
		// *** DiffTestPgm1 ***
		// ********************
		// ** SPACE property
		// 0x10018ae: space=1.
		// 0x10018ba: space=1.
		// 0x10018ff: space=1.
		// 0x100248c: space=1.
		// ** testColor property
		// 0x100248c: testColor=CYAN.
		// 0x10039dd: testColor=BLACK.
		// 0x10039f8: testColor=BLACK.
		// 0x10039fe: testColor=RED.
		mtf.initialize("DiffTestPgm1", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				assertNotNull(opm);
				opm.add(addr(program, "0x1002400"), new SaveableColor(Color.GRAY));
				opm.add(addr(program, "0x1002466"), new SaveableColor(Color.CYAN));
				opm.add(addr(program, "0x1002472"), new SaveableColor(Color.BLACK));
				opm.add(addr(program, "0x1002488"), new SaveableColor(Color.PINK));
				opm.add(addr(program, "0x100248a"), new SaveableColor(Color.GREEN));
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.WHITE));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				assertNotNull(opm);
				opm.add(addr(program, "0x1002466"), new SaveableColor(Color.MAGENTA));
				opm.add(addr(program, "0x1002481"), new SaveableColor(Color.RED));
				opm.add(addr(program, "0x1002488"), new SaveableColor(Color.PINK));
				opm.add(addr(program, "0x100248a"), new SaveableColor(Color.CYAN));
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.YELLOW));
				opm.add(addr(program, "0x1002490"), new SaveableColor(Color.ORANGE));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x1002466"), "testColor", KEEP_ORIGINAL);
		chooseUserDefined(addr("0x100248a"), "testColor", KEEP_LATEST);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_MY);
		waitForMergeCompletion();

		PropertyMapManager pmm;
		ObjectPropertyMap<?> opm;
		pmm = resultProgram.getUsrPropertyManager();
		opm = pmm.getObjectPropertyMap("testColor");
		Address address = resultProgram.getMinAddress();

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002400"), address);
		assertEquals(new SaveableColor(Color.GRAY), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002472"), address);
		assertEquals(new SaveableColor(Color.BLACK), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002481"), address);
		assertEquals(new SaveableColor(Color.RED), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002488"), address);
		assertEquals(new SaveableColor(Color.PINK), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248a"), address);
		assertEquals(new SaveableColor(Color.GREEN), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(new SaveableColor(Color.YELLOW), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002490"), address);
		assertEquals(new SaveableColor(Color.ORANGE), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039dd"), address);
		assertEquals(new SaveableColor(Color.BLACK), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.BLACK), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039fe"), address);
		assertEquals(new SaveableColor(Color.RED), opm.get(address));

		address = opm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testAddStringProperty() throws Exception {
		mtf.initialize("DiffTestPgm1", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap spm = pmm.createStringPropertyMap("testString");
					spm.add(addr(program, "0x1002400"), "Eleven");
					spm.add(addr(program, "0x1002466"), "Six");
					spm.add(addr(program, "0x1002472"), "Twelve");
					spm.add(addr(program, "0x1002488"), "EightyEight");
					spm.add(addr(program, "0x100248a"), "Fourteen");
					spm.add(addr(program, "0x100248c"), "Fifteen");
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap spm = pmm.createStringPropertyMap("testString");
					spm.add(addr(program, "0x1002481"), "TwentyOne");
					spm.add(addr(program, "0x1002466"), "SixtySix");
					spm.add(addr(program, "0x1002488"), "EightyEight");
					spm.add(addr(program, "0x100248a"), "TwentyThree");
					spm.add(addr(program, "0x100248c"), "TwentyFour");
					spm.add(addr(program, "0x1002490"), "TwentyFive");
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x1002466"), "testString", KEEP_ORIGINAL);
		chooseUserDefined(addr("0x100248a"), "testString", KEEP_LATEST);
		chooseUserDefined(addr("0x100248c"), "testString", KEEP_MY);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		StringPropertyMap spm = pmm.getStringPropertyMap("testString");
		Address address = resultProgram.getMinAddress();

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002400"), address);
		assertEquals("Eleven", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002472"), address);
		assertEquals("Twelve", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002481"), address);
		assertEquals("TwentyOne", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002488"), address);
		assertEquals("EightyEight", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248a"), address);
		assertEquals("Fourteen", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals("TwentyFour", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002490"), address);
		assertEquals("TwentyFive", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testAddStringProperty2() throws Exception {
		mtf.initialize("DiffTestPgm1", new OriginalProgramModifierListener() {

			@Override
			public void modifyOriginal(ProgramDB program) throws Exception {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap spm = pmm.createStringPropertyMap("testString1");
					spm.add(addr(program, "0x1002100"), "A");
					spm.add(addr(program, "0x1002166"), "B");

					spm = pmm.createStringPropertyMap("testString2");
					spm.add(addr(program, "0x1002200"), "C");
					spm.add(addr(program, "0x1002266"), "D");
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap spm = pmm.createStringPropertyMap("testString3");
					spm.add(addr(program, "0x1002381"), "E");
					spm.add(addr(program, "0x1002366"), "F");

					// Add to existing map removed in latest (conflict)
					// NOTE: In the absence of property conflict handling this
					// will cause testString1 properties removed in latest to be
					// re-introduced into result

					// TODO: improve property conflict handling

					spm = pmm.getStringPropertyMap("testString1");
					spm.add(addr(program, "0x1002500"), "G");
					spm.add(addr(program, "0x1002566"), "H");
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap spm = pmm.createStringPropertyMap("testString4");
					spm.add(addr(program, "0x1002500"), "I");
					spm.add(addr(program, "0x1002566"), "J");

					pmm.removePropertyMap("testString1");

				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

		});

		executeMerge(ASK_USER);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		StringPropertyMap spm = pmm.getStringPropertyMap("testString1");
		Address address = resultProgram.getMinAddress();

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002500"), address);
		assertEquals("G", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002566"), address);
		assertEquals("H", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertNull(address);

		spm = pmm.getStringPropertyMap("testString2");
		address = resultProgram.getMinAddress();

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002200"), address);
		assertEquals("C", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002266"), address);
		assertEquals("D", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertNull(address);

		spm = pmm.getStringPropertyMap("testString3");
		address = resultProgram.getMinAddress();

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002366"), address);
		assertEquals("F", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002381"), address);
		assertEquals("E", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertNull(address);

		spm = pmm.getStringPropertyMap("testString4");
		address = resultProgram.getMinAddress();

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002500"), address);
		assertEquals("I", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002566"), address);
		assertEquals("J", spm.getString(address));

		address = spm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testAddStringProperty3() throws Exception {
		mtf.initialize("DiffTestPgm1", new OriginalProgramModifierListener() {

			@Override
			public void modifyOriginal(ProgramDB program) throws Exception {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					StringPropertyMap pm = pmm.createStringPropertyMap("testMap");
					pm.add(addr(program, "0x1002100"), "A");
					pm.add(addr(program, "0x1002166"), "B");
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					pmm.removePropertyMap("testMap");

					IntPropertyMap pm = pmm.createIntPropertyMap("testMap");
					pm.add(addr(program, "0x1002100"), 1);
					pm.add(addr(program, "0x1002166"), 2);
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				try {
					pmm.removePropertyMap("testMap");

					LongPropertyMap pm = pmm.createLongPropertyMap("testMap");
					pm.add(addr(program, "0x1002100"), 1);
					pm.add(addr(program, "0x1002166"), 2);
				}
				catch (DuplicateNameException e) {
					e.printStackTrace();
				}
			}

		});

		executeMerge(ASK_USER);

		AbstractErrDialog errDlg = waitForErrorDialog();
		String message = errDlg.getMessage();
		assertEquals("Latest and Checked Out program versions do not have the same type for " +
			"'testMap' property.", message);
		pressButtonByText(errDlg, "OK");

		chooseUserDefined(addr("0x1002100"), "testMap", KEEP_ORIGINAL);
		chooseUserDefined(addr("0x1002166"), "testMap", KEEP_LATEST);

		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		PropertyMap<?> pm = pmm.getPropertyMap("testMap");
		Address address = resultProgram.getMinAddress();

		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002166"), address);
		assertEquals(2L, pm.get(address));

		// TODO: Incompatible maps result in silently discarded property values selected during
		// merge.  This should be handled at map-level and not as a address-level conflict.
		// (See GP-2585)

		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testRemoveSpaceProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.remove(addr(program, "0x10018ba"));
				pm.remove(addr(program, "0x1002428"));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.remove(addr(program, "0x10018ce"));
				pm.remove(addr(program, "0x1002428"));
			}
		});

		executeMerge(ASK_USER);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testRemoveColorProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.remove(addr(program, "0x100248c"));
				pm.remove(addr(program, "0x10039f8"));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.remove(addr(program, "0x10039f1"));
				pm.remove(addr(program, "0x10039f8"));
			}
		});

		executeMerge(ASK_USER);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039fe"), address);
		assertEquals(new SaveableColor(Palette.GREEN), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testRemoveVsChangeSpaceProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.remove(addr(program, "0x10018ba"));
				pm.remove(addr(program, "0x10018ce"));
				pm.remove(addr(program, "0x10018ff"));
				pm.add(addr(program, "0x1002428"), 4);
				pm.add(addr(program, "0x100248c"), 5);
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x10018ba"), 5);
				pm.add(addr(program, "0x10018ce"), 1);
				pm.add(addr(program, "0x10018ff"), 1);
				pm.remove(addr(program, "0x1002428"));
				pm.remove(addr(program, "0x100248c"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10018ba"), "Space", KEEP_LATEST); // Remove
		chooseUserDefined(addr("0x10018ce"), "Space", KEEP_MY);     // Change
		chooseUserDefined(addr("0x10018ff"), "Space", KEEP_ORIGINAL); // Original
		chooseUserDefined(addr("0x1002428"), "Space", KEEP_LATEST); // Change
		chooseUserDefined(addr("0x100248c"), "Space", KEEP_MY);     // Remove
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002428"), address);
		assertEquals(4, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testRemoveVsChangeTestColorProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.remove(addr(program, "0x100248c"));
				pm.remove(addr(program, "0x10039f1"));
				pm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				pm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				pm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				pm.remove(addr(program, "0x10039f8"));
				pm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_LATEST); // Remove
		chooseUserDefined(addr("0x10039f1"), "testColor", KEEP_MY);     // Change
		chooseUserDefined(addr("0x10039f8"), "testColor", KEEP_LATEST); // Change
		chooseUserDefined(addr("0x10039fe"), "testColor", KEEP_MY);     // Remove
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Color.ORANGE), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.BLUE), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testChangeSpaceProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x10018ba"), 3);
				pm.add(addr(program, "0x10018ff"), 1);
				pm.add(addr(program, "0x1002428"), 4);
				pm.add(addr(program, "0x100248c"), 5);
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x10018ce"), 5);
				pm.add(addr(program, "0x10018ff"), 1);
				pm.add(addr(program, "0x1002428"), 3);
				pm.add(addr(program, "0x100248c"), 6);
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x1002428"), "testColor", KEEP_LATEST);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_MY);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(3, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(5, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002428"), address);
		assertEquals(4, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(6, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testChangeTestColorProperty() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.add(addr(program, "0x100248c"), new SaveableColor(Color.BLUE));
				pm.add(addr(program, "0x10039f8"), new SaveableColor(Color.ORANGE));
				pm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.add(addr(program, "0x10039f1"), new SaveableColor(Color.RED));
				pm.add(addr(program, "0x10039f8"), new SaveableColor(Color.ORANGE));
				pm.add(addr(program, "0x10039fe"), new SaveableColor(Color.YELLOW));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10039fe"), "testColor", KEEP_MY);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(new SaveableColor(Color.BLUE), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Color.RED), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.ORANGE), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039fe"), address);
		assertEquals(new SaveableColor(Color.YELLOW), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testApplyToAllSpacePropertyConflicts() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.remove(addr(program, "0x10018ba"));
				pm.remove(addr(program, "0x10018ce"));
				pm.add(addr(program, "0x1002428"), 4);
				pm.add(addr(program, "0x100248c"), 5);
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				IntPropertyMap pm = pmm.getIntPropertyMap("Space");
				pm.add(addr(program, "0x10018ba"), 5);
				pm.add(addr(program, "0x10018ce"), 1);
				pm.remove(addr(program, "0x1002428"));
				pm.remove(addr(program, "0x100248c"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10018ba"), "testColor", KEEP_LATEST);
		chooseUserDefined(addr("0x10018ce"), "testColor", KEEP_MY, true);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testApplyToAllColorPropertyConflicts() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.remove(addr(program, "0x100248c"));
				pm.remove(addr(program, "0x10039f1"));
				pm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				pm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();
				ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
				pm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				pm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				pm.remove(addr(program, "0x10039f8"));
				pm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_MY, true);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();
		ObjectPropertyMap<?> pm = pmm.getObjectPropertyMap("testColor");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(new SaveableColor(Color.RED), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Color.ORANGE), pm.get(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testMultiplePropertyTypeConflicts() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.add(addr(program, "0x10018ba"), 3);
				ipm.add(addr(program, "0x10018ff"), 1);
				ipm.add(addr(program, "0x1002428"), 4);
				ipm.add(addr(program, "0x100248c"), 5);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.remove(addr(program, "0x100248c"));
				opm.remove(addr(program, "0x10039f1"));
				opm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				opm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.add(addr(program, "0x10018ce"), 5);
				ipm.add(addr(program, "0x10018ff"), 1);
				ipm.add(addr(program, "0x1002428"), 3);
				ipm.add(addr(program, "0x100248c"), 6);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				opm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				opm.remove(addr(program, "0x10039f8"));
				opm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x1002428"), "testColor", KEEP_LATEST);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_MY);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_LATEST); // Remove
		chooseUserDefined(addr("0x10039f1"), "testColor", KEEP_MY);     // Change
		chooseUserDefined(addr("0x10039f8"), "testColor", KEEP_LATEST); // Change
		chooseUserDefined(addr("0x10039fe"), "testColor", KEEP_MY);     // Remove
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = ipm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(3, ipm.getInt(address));
		address = ipm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(5, ipm.getInt(address));
		address = ipm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(1, ipm.getInt(address));
		address = ipm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002428"), address);
		assertEquals(4, ipm.getInt(address));
		address = ipm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(6, ipm.getInt(address));
		address = ipm.getNextPropertyAddress(address);
		assertNull(address);

		ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
		address = resultProgram.getMinAddress();
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Color.ORANGE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.BLUE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testApplyToAllSpaceForMultipleTypeConflicts() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.remove(addr(program, "0x10018ba"));
				ipm.remove(addr(program, "0x10018ce"));
				ipm.add(addr(program, "0x1002428"), 4);
				ipm.add(addr(program, "0x100248c"), 5);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.remove(addr(program, "0x100248c"));
				opm.remove(addr(program, "0x10039f1"));
				opm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				opm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.add(addr(program, "0x10018ba"), 5);
				ipm.add(addr(program, "0x10018ce"), 1);
				ipm.remove(addr(program, "0x1002428"));
				ipm.remove(addr(program, "0x100248c"));

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				opm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				opm.remove(addr(program, "0x10039f8"));
				opm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10018ba"), "Space", KEEP_MY, true);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_LATEST); // Remove
		chooseUserDefined(addr("0x10039f1"), "testColor", KEEP_MY);     // Change
		chooseUserDefined(addr("0x10039f8"), "testColor", KEEP_LATEST); // Change
		chooseUserDefined(addr("0x10039fe"), "testColor", KEEP_MY);     // Remove
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(5, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);

		ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
		address = resultProgram.getMinAddress();
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Color.ORANGE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.BLUE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testApplyToAllForEachConflict() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.remove(addr(program, "0x10018ba"));
				ipm.remove(addr(program, "0x10018ce"));
				ipm.add(addr(program, "0x1002428"), 4);
				ipm.add(addr(program, "0x100248c"), 5);
				ipm.add(addr(program, "0x10061e0"), 2);
				ipm.add(addr(program, "0x10018ea"), 1);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.remove(addr(program, "0x100248c"));
				opm.remove(addr(program, "0x10039f1"));
				opm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				opm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.add(addr(program, "0x10018ba"), 5);
				ipm.add(addr(program, "0x10018ce"), 1);
				ipm.remove(addr(program, "0x1002428"));
				ipm.remove(addr(program, "0x100248c"));
				ipm.add(addr(program, "0x10061e0"), 3);
				ipm.add(addr(program, "0x10018ea"), 4);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				opm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				opm.remove(addr(program, "0x10039f8"));
				opm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10018ba"), "Space", KEEP_MY, true);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_LATEST, true);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(5, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ea"), address);
		assertEquals(4, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10061e0"), address);
		assertEquals(3, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);

		ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
		address = resultProgram.getMinAddress();
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Color.BLUE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039fe"), address);
		assertEquals(new SaveableColor(Color.PINK), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertNull(address);
	}

	@Test
	public void testApplyToAllForEachConflictPickOriginal() throws Exception {
		// ** DiffTestPgm2 **
		// 10018ba = 1
		// 10018ce = 2
		// 10018ff = 2
		// 1002428 = 1
		// 100248c = 1 , WHITE(255,255,255)
		// 10039f1 = BLACK(0,0,0)
		// 10039f8 = BLACK(0,0,0)
		// 10039fe = GREEN(0,255,0)

		mtf.initialize("DiffTestPgm2", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.remove(addr(program, "0x10018ba"));
				ipm.remove(addr(program, "0x10018ce"));
				ipm.add(addr(program, "0x1002428"), 4);
				ipm.add(addr(program, "0x100248c"), 5);
				ipm.add(addr(program, "0x10061e0"), 2);
				ipm.add(addr(program, "0x10018ea"), 1);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.remove(addr(program, "0x100248c"));
				opm.remove(addr(program, "0x10039f1"));
				opm.add(addr(program, "0x10039f8"), new SaveableColor(Color.BLUE));
				opm.add(addr(program, "0x10039fe"), new SaveableColor(Color.PINK));
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				PropertyMapManager pmm = program.getUsrPropertyManager();

				IntPropertyMap ipm = pmm.getIntPropertyMap("Space");
				ipm.add(addr(program, "0x10018ba"), 5);
				ipm.add(addr(program, "0x10018ce"), 1);
				ipm.remove(addr(program, "0x1002428"));
				ipm.remove(addr(program, "0x100248c"));
				ipm.add(addr(program, "0x10061e0"), 3);
				ipm.add(addr(program, "0x10018ea"), 4);

				ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
				opm.add(addr(program, "0x100248c"), new SaveableColor(Color.RED));
				opm.add(addr(program, "0x10039f1"), new SaveableColor(Color.ORANGE));
				opm.remove(addr(program, "0x10039f8"));
				opm.remove(addr(program, "0x10039fe"));
			}
		});

		executeMerge(ASK_USER);
		chooseUserDefined(addr("0x10018ba"), "Space", KEEP_ORIGINAL, true);
		chooseUserDefined(addr("0x100248c"), "testColor", KEEP_ORIGINAL, true);
		waitForMergeCompletion();

		PropertyMapManager pmm = resultProgram.getUsrPropertyManager();

		IntPropertyMap pm = pmm.getIntPropertyMap("Space");
		Address address = resultProgram.getMinAddress();
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ba"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ce"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x10018ff"), address);
		assertEquals(2, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x1002428"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(1, pm.getInt(address));
		address = pm.getNextPropertyAddress(address);
		assertNull(address);

		ObjectPropertyMap<?> opm = pmm.getObjectPropertyMap("testColor");
		address = resultProgram.getMinAddress();
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x100248c"), address);
		assertEquals(new SaveableColor(Palette.WHITE), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f1"), address);
		assertEquals(new SaveableColor(Palette.BLACK), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039f8"), address);
		assertEquals(new SaveableColor(Palette.BLACK), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertEquals(addr("0x10039fe"), address);
		assertEquals(new SaveableColor(Palette.GREEN), opm.get(address));
		address = opm.getNextPropertyAddress(address);
		assertNull(address);
	}

}
