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
package db.util;

import java.io.IOException;

/**
 * Report database errors.
 */
public interface ErrorHandler {
	
	/**
	 * Notification that an IO exception occurred.
	 * 
	 * @param e {@link IOException} which was cause of error
	 * @throws RuntimeException optional exception which may be thrown when
	 * responding to error condition.
	 */
	public void dbError(IOException e) throws RuntimeException;
}
