/*   RegisterAdapter
 *   Used to add additional datatypes to the Register
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Oct 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.lang.reflect.Field;

/**
 * Used to add additional datatypes to the Register
 * Add an implementing class to the Register instance using Register.getRegister().addAdapter(registerAdapter)
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public interface RegisterAdapterIF {
    /**
     * Used to defined custom data types of fields not defined in Register
     * Override to return the type enum. These enums must be unique and greater than 0
     * This method is checked BEFORE the field is assigned by Register.
     * For field types not set by this implementation, return Register.UNKNOWN
     */
    public short getFieldClassType(Field field);
}