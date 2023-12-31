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
package ghidra.app.merge.datatypes;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import ghidra.program.database.ProgramDB;
import ghidra.program.database.ProgramModifierListener;
import ghidra.program.model.data.*;
import ghidra.util.InvalidNameException;
import ghidra.util.exception.DuplicateNameException;
import ghidra.util.task.TaskMonitor;

/**
 * Tests for the merge data type manager.
 */
public class CategoryMerge1Test extends AbstractDataTypeMergeTest {

	@Test
    public void testCategoryRenamedNoConflicts() throws Exception {
		// Rename category in My program; no change in Results program
		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				// Make no changes to Latest.
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}
		});

		executeMerge(-1);
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		assertNotNull(root.getCategory("My Misc"));
		assertNull(root.getCategory("MISC"));
	}

	@Test
    public void testCategoryMovedNoConflicts() throws Exception {
		// Move category in My program; no change in latest program

		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				// Make no changes to Latest.
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
					Structure s =
						new StructureDataType(miscCat.getCategoryPath(), "My structure", 0);
					s.add(new ByteDataType());
					s.add(new WordDataType());
					destCat.addDataType(s, DataTypeConflictHandler.DEFAULT_HANDLER);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});

		executeMerge(-1);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);

		assertTrue(dtm.containsCategory(new CategoryPath("/Category1/Category2/Category3/MISC")));
		assertNotNull(
			dtm.getDataType(new CategoryPath("/Category1/Category2/Category3"), "My structure"));
		assertNull(root.getCategory("MISC"));
		assertTrue(!dtm.containsCategory(new CategoryPath("/MISC")));
	}

	@Test
    public void testCategoryRenamedInLatest() throws Exception {

		// A category was renamed in the latest; not changed in my program
		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move /Category1/Category2/Category5 to
				// /Category1/Category2/Category3
				Category c = dtm.getCategory(new CategoryPath("/Category1/Category2/Category5"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(c, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});

		executeMerge(-1);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		assertNotNull(root.getCategory("My Misc"));
		assertNull(root.getCategory("MISC"));

		Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
		assertNotNull(destCat.getCategory("Category5"));

		Category c = dtm.getCategory(new CategoryPath("/Category1/Category2/Category4"));
		assertNull(c.getCategory("Category5"));
	}

	@Test
    public void testCategoryRenamedToSame() throws Exception {

		// A category was renamed in the latest; not changed in my program
		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				// move /Category1/Category2/Category5 to
				// /Category1/Category2/Category3
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}
		});

		executeMerge(-1);
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		assertNotNull(root.getCategory("My Misc"));
		assertNull(root.getCategory("MISC"));
	}

	@Test
    public void testCategoryAddedInLatest() throws Exception {

		// A category was added to Category5 in the latest; 
		// in My program, rename Category5 to "My Category5"
		mtf.initialize("notepad", new ProgramModifierListener() {
	
			@Override
			public void modifyLatest(ProgramDB program) {
				Category c = program.getDataTypeManager().getCategory(
					new CategoryPath("/Category1/Category2/Category5"));
				try {
					c.createCategory("AnotherCategory");
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move /Category1/Category2/Category5 to
				// /Category1/Category2/Category3
				Category c = dtm.getCategory(new CategoryPath("/Category1/Category2/Category5"));
				try {
					c.setName("My Category5");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
				catch (InvalidNameException e) {
					Assert.fail("Got InvalidNameException!");
				}
			}
		});

		executeMerge(-1);
		// should end up with /Category1/Category2/My Category5/AnotherCategory
		DataTypeManager dtm = resultProgram.getDataTypeManager();

		Category c = dtm.getCategory(new CategoryPath("/Category1/Category2"));
		c = c.getCategory("My Category5");
		assertNotNull(c);
		assertNotNull(c.getCategory("AnotherCategory"));
	}

	@Test
    public void testCategoryRenamedInBoth() throws Exception {

		// A category was renamed in the latest and changed in my program
		mtf.initialize("notepad", new ProgramModifierListener() {

			@Override
			public void modifyLatest(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("Some Other Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}
		});
		// choose My program
		executeMerge(DataTypeMergeManager.OPTION_MY);
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		// expect category "Some Other Misc" to exist in results program
		assertNull(root.getCategory("MISC"));
		assertNotNull(root.getCategory("Some Other Misc"));
	}

	@Test
    public void testCategoryRenamedInBoth2() throws Exception {

		// A category was renamed in the latest and changed in my program
		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				// change the name
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("Some Other Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}
		});
		// choose Latest
		executeMerge(DataTypeMergeManager.OPTION_LATEST);
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		// expect category "Some Other Misc" to exist in results program
		assertNotNull(root.getCategory("My Misc"));
		assertNull(root.getCategory("Some Other Misc"));
	}

	@Test
    public void testCategoryRenamedInBoth3() throws Exception {

		// A category was renamed in the latest and changed in my program
		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				// change the name
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("My Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				Category miscCat =
					program.getDataTypeManager().getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("Some Other Misc");
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception! " + e.getMessage());
				}
				catch (InvalidNameException e) {
					Assert.fail("Got Invalid Name Exception! " + e.getMessage());
				}
			}
		});
		// choose original
		executeMerge(DataTypeMergeManager.OPTION_ORIGINAL);
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		// expect category "Some Other Misc" to exist in results program
		assertNotNull(root.getCategory("MISC"));
		assertNull(root.getCategory("My Misc"));
		assertNull(root.getCategory("Some Other Misc"));
	}

	@Test
    public void testCategoryMovedConflicts() throws Exception {
		// Move category in My program; move same category in Latest program

		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});
		// choose my program change
		executeMerge(DataTypeMergeManager.OPTION_MY);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
		assertNotNull(destCat.getCategory("MISC"));
		assertNull(root.getCategory("MISC"));
		Category c = dtm.getCategory(new CategoryPath("/Category1/Category2"));
		assertNull(c.getCategory("MISC"));
	}

	@Test
    public void testCategoryMovedConflicts2() throws Exception {
		// Move category in My program; move same category in Latest program

		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});
		// choose my Latest change
		executeMerge(DataTypeMergeManager.OPTION_LATEST);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
		assertNull(destCat.getCategory("MISC"));
		assertNull(root.getCategory("MISC"));
		Category c = dtm.getCategory(new CategoryPath("/Category1/Category2"));
		assertNotNull(c.getCategory("MISC"));
	}

	@Test
    public void testCategoryMovedConflicts3() throws Exception {
		// Move category in My program; move same category in Latest program

		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});
		// choose my ORIGINAL
		executeMerge(DataTypeMergeManager.OPTION_ORIGINAL);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
		assertNull(destCat.getCategory("MISC"));
		assertNotNull(root.getCategory("MISC"));
		Category c = dtm.getCategory(new CategoryPath("/Category1/Category2"));
		assertNull(c.getCategory("MISC"));
	}

	@Test
    public void testCategoryMoved() throws Exception {
		// Move category in My program; move same category in Latest program

		mtf.initialize("notepad", new ProgramModifierListener() {
			@Override
			public void modifyLatest(ProgramDB program) {
				// Make no changes to Latest.
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Category destCat =
					dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
				try {
					destCat.moveCategory(miscCat, TaskMonitor.DUMMY);
				}
				catch (DuplicateNameException e) {
					Assert.fail("Got Duplicate name exception!");
				}
			}
		});
		executeMerge(-1);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		Category root = resultProgram.getDataTypeManager().getCategory(CategoryPath.ROOT);
		Category destCat = dtm.getCategory(new CategoryPath("/Category1/Category2/Category3"));
		assertNotNull(destCat.getCategory("MISC"));
		assertNull(root.getCategory("MISC"));
	}

	@Test
    public void testCategoryAddedDeleted() throws Exception {
		// Add/delete category in My progrma

		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				// Make no changes to Latest.
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();

				CategoryPath path = new CategoryPath("/MyCategory");
				try {
					dtm.createCategory(path);
					dtm.getRootCategory().removeCategory("MyCategory",
						TaskMonitor.DUMMY);
				}
				catch (Exception e) {
					Assert.fail("Got exception: " + e);
				}
			}
		});
		executeMerge(-1);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		assertNull(dtm.getRootCategory().getCategory("MyCategory"));
	}

	@Test
    public void testDataTypeAdded() throws Exception {
		// LATEST: rename /MISC to /Other_MISC
		// MY: add new data type to /MISC

		mtf.initialize("notepad", new ProgramModifierListener() {
			
			@Override
			public void modifyLatest(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				try {
					miscCat.setName("Other_MISC");
				}
				catch (Exception e) {
					Assert.fail("Got exception: " + e);
				}
			}

			@Override
			public void modifyPrivate(ProgramDB program) {
				DataTypeManager dtm = program.getDataTypeManager();
				// move MISC to /Category1/Category2/Category3
				Category miscCat = dtm.getCategory(new CategoryPath("/MISC"));
				Structure s1 = new StructureDataType(miscCat.getCategoryPath(), "struct_one", 0);
				s1.add(new ByteDataType());
				s1.add(new FloatDataType());
				try {
					miscCat.addDataType(s1, DataTypeConflictHandler.DEFAULT_HANDLER);
				}
				catch (Exception e) {
					Assert.fail("Got exception: " + e);
				}
			}
		});
		executeMerge(-1);
		DataTypeManager dtm = resultProgram.getDataTypeManager();
		assertTrue(dtm.containsCategory(new CategoryPath("/MISC")));
		assertTrue(dtm.containsCategory(new CategoryPath("/Other_MISC")));
		assertNotNull(dtm.getDataType(new CategoryPath("/MISC"), "struct_one"));
	}

}
