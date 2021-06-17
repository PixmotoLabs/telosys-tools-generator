/**
 *  Copyright (C) 2008-2017  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.tools.generator.context;

import org.telosys.tools.commons.DatabaseUtil;
import org.telosys.tools.commons.StrUtil;
import org.telosys.tools.commons.jdbctypes.JdbcTypes;
import org.telosys.tools.commons.jdbctypes.JdbcTypesManager;
import org.telosys.tools.generator.GeneratorException;
import org.telosys.tools.generator.GeneratorUtil;
import org.telosys.tools.generator.context.doc.VelocityMethod;
import org.telosys.tools.generator.context.doc.VelocityObject;
import org.telosys.tools.generator.context.names.ContextName;
import org.telosys.tools.generic.model.Attribute;
import org.telosys.tools.generic.model.DateType;
import org.telosys.tools.generic.model.types.AttributeTypeInfo;
import org.telosys.tools.generic.model.types.LanguageType;
import org.telosys.tools.generic.model.types.NeutralType;
import org.telosys.tools.generic.model.types.TypeConverter;


/**
 * Context class for a BEAN ATTRIBUTE ( with or without database mapping )
 *  
 * @author Laurent GUERIN
 */
//-------------------------------------------------------------------------------------
@VelocityObject(
		contextName = ContextName.ATTRIBUTE ,
		otherContextNames= { ContextName.ATTRIB, ContextName.FIELD },		
		text = {
				"This object provides all information about an entity attribute",
				"Each attribute is retrieved from the entity class ",
				""
		},
		since = "",
		example= {
				"",
				"#foreach( $attribute in $entity.attributes )",
				"    $attribute.name : $attribute.type",
				"#end"
		}
		
 )
//-------------------------------------------------------------------------------------
public class AttributeInContext 
{
//    public final static int NO_DATE_TYPE   = 0 ;
//    public final static int DATE_ONLY      = 1 ;
//    public final static int TIME_ONLY      = 2 ;
//    public final static int DATE_AND_TIME  = 3 ;
    
    private final static String VOID_STRING  = "" ;
    
//    private final static String TYPE_INT  = "int" ;
//    private final static String TYPE_NUM  = "num" ;
//    private final static String TYPE_DATE = "date" ;
//    private final static String TYPE_TIME = "time" ;
    
	private final EnvInContext     _env ; // ver 3.0.0

	//--- 
    private final EntityInContext  _entity ; // The entity 
    
	private final ModelInContext   _modelInContext ;  // v 3.0.0

    
	//--- Basic minimal attribute info -------------------------------------------------
	private final String  _sName ;  // attribute name 
	
//	private final String  _sSimpleType ;  // Short java type without package, without blank, eg : "int", "BigDecimal", "Date"
//	private final String  _sFullType ;    // Full java type with package, : "java.math.BigDecimal", "java.util.Date"
	private final String  _sNeutralType ;    //v 3.0.0
	private final AttributeTypeInfo attributeTypeInfo ; // v 3.0.0
	
	
	private boolean       _bUseFullType = false ;
	
	private final String  _sInitialValue ; // can be null 
	
	private final String  _sDefaultValue ; // can be null 
	private final boolean _bSelected ; // v 2.1.1 #LGU
	private final boolean _bInsertable ;
	private final boolean _bUpdatable ;
	
	//--- Database info -------------------------------------------------
    private final boolean _bKeyElement      ;  // True if primary key
    
    // Removed in  v 3.0.0
    // private final boolean _bUsedInForeignKey    ;
    private final boolean _bForeignKey          ; // v 3.0.0
    private final boolean _bForeignKeySimple    ; // v 3.0.0
    private final boolean _bForeignKeyComposite ; // v 3.0.0
    private final String  _sReferencedEntityClassName ; // v 3.0.0
    
    private final boolean _bAutoIncremented  ;  // True if auto-incremented by the database
    private final String  _sDataBaseName     ;  // Column name in the DB table
    private final String  _sDataBaseType      ;  // Column type in the DB table
    private final int     _iJdbcTypeCode    ;     // JDBC type code for this column
    private final String  _sJdbcTypeName    ;  // JDBC type name : added in ver 2.0.7
    private final int     _iDatabaseSize   ;     // Size of this column (if Varchar ) etc..
    private final String  _sDatabaseComment ;     // Comment of this column ( v 2.1.1 - #LCH )
    private final String  _sDatabaseDefaultValue ; // keep null (do not initialize to "" )  
    private final boolean _bDatabaseNotNull ;  // True if "not null" in the database
    
    //--- Further info for ALL ---------------------------------------
    private final boolean _bNotNull   ;
    private final String  _sLabel     ; // v 2.0.3
    private final String  _sInputType ; // v 2.0.3
    

    //--- Further info for BOOLEAN -----------------------------------
    private final String  _sBooleanTrueValue  ; // eg "1", ""Yes"", ""true""
    private final String  _sBooleanFalseValue ; // eg "0", ""No"",  ""false""
    
    //--- Further info for DATE/TIME ---------------------------------
    private final DateType _dateType       ;  // By default only DATE
    private final boolean  _bDatePast        ;
    private final boolean  _bDateFuture     ;
    private final boolean  _bDateBefore      ;
    private final String   _sDateBeforeValue  ;
    private final boolean  _bDateAfter       ;
    private final String   _sDateAfterValue   ;

    //--- Further info for NUMBER ------------------------------------
    private final String  _sMinValue ; 
    private final String  _sMaxValue ; 

    //--- Further info for STRING ------------------------------------
    private final boolean _bLongText   ;  // True if must be stored as a separate tag in the XML flow
    private final boolean _bNotEmpty   ;
    private final boolean _bNotBlank   ;
    private final String  _sMinLength  ; 
    private final String  _sMaxLength  ; 
    private final String  _sPattern    ; 
    
	//--- JPA KEY Generation infos -------------------------------------------------
    private final boolean _bGeneratedValue ;  // True if GeneratedValue ( annotation "@GeneratedValue" )
	private final String  _sGeneratedValueStrategy ; // "AUTO", "IDENTITY", "SEQUENCE", "TABLE" 
	private final String  _sGeneratedValueGenerator ;
	
    private final boolean _bSequenceGenerator  ;  // True if SequenceGenerator ( annotation "@SequenceGenerator" )
	private final String  _sSequenceGeneratorName     ;
	private final String  _sSequenceGeneratorSequenceName   ;
	private final int     _iSequenceGeneratorAllocationSize ;

    private final boolean _bTableGenerator ;  // True if TableGenerator ( annotation "@TableGenerator" )
	private final String  _sTableGeneratorName  ;
	private final String  _sTableGeneratorTable           ;
	private final String  _sTableGeneratorPkColumnName     ;
	private final String  _sTableGeneratorValueColumnName  ;
	private final String  _sTableGeneratorPkColumnValue   ;

	private final boolean _bIsUsedInLinks ; // v 3.0.0 #LGU
	private final boolean _bIsUsedInSelectedLinks ; // v 3.0.0 #LGU
	
	//-----------------------------------------------------------------------------------------------
	//public AttributeInContext(final EntityInContext entity, final Column column) 
	/**
	 * Constructor to create an ATTRIBUTE in the generator context
	 * @param entity
	 * @param attribute
	 * @param env
	 */
	public AttributeInContext(final EntityInContext entity, 
			final Attribute attribute, 
			final ModelInContext modelInContext, // v 3.0.0
			final EnvInContext env) // v 3.0.0
	{
		_env = env ; // v 3.0.0
		_modelInContext = modelInContext ; // v 3.0.0

		_entity = entity ;
		
		//_sName   = column.getJavaName();
		_sName   = attribute.getName(); // v 3.0.0
		
//		//_sFullType   = StrUtil.removeAllBlanks( column.getJavaType() );
//		_sFullType   = StrUtil.removeAllBlanks( attribute.getFullType() ); // v 3.0.0
//		_sSimpleType = JavaClassUtil.shortName( _sFullType );    // v 2.0.7
		_sNeutralType     = attribute.getNeutralType() ; // v 3.0.0
		this.attributeTypeInfo = new AttributeTypeInfo(attribute) ; // v 3.0.0
				
		//_bSelected        = column.getSelected(); // v 2.1.1 #LGU
		_bSelected        = attribute.isSelected(); // v 3.0.0
		_bInsertable      = attribute.isInsertable();
		_bUpdatable       = attribute.isUpdatable();
		
		//_sInitialValue    = null ; //  column.getJavaInitialValue()  ???
		_sInitialValue    = StrUtil.notNull( attribute.getInitialValue() ); // v 3.0.0
		//_sDefaultValue    = column.getJavaDefaultValue();
		_sDefaultValue    = StrUtil.notNull( attribute.getDefaultValue() ); // v 3.0.0
		
		_sDataBaseName     = StrUtil.notNull( attribute.getDatabaseName() ) ;
        //_sDataBaseType     = column.getDatabaseTypeName() ;
        _sDataBaseType     = StrUtil.notNull( attribute.getDatabaseType() ) ; // v 3.0.0
        _iJdbcTypeCode     = attribute.getJdbcTypeCode() != null ? attribute.getJdbcTypeCode() : 0 ; // v 3.0.0
        _sJdbcTypeName     = StrUtil.notNull( attribute.getJdbcTypeName() );
        //_bKeyElement       = column.isPrimaryKey() ;
        _bKeyElement       = attribute.isKeyElement(); // v 3.0.0
        //_bUsedInForeignKey = column.isForeignKey(); 
        
        //_bUsedInForeignKey = attribute.isUsedInForeignKey() ; // v 3.0.0
        _bForeignKey          = attribute.isFK() ; // v 3.0.0
        _bForeignKeySimple    = attribute.isFKSimple() ; // v 3.0.0
        _bForeignKeyComposite = attribute.isFKComposite() ; // v 3.0.0
        _sReferencedEntityClassName = attribute.getReferencedEntityClassName() ;  // v 3.0.0

        _bAutoIncremented  = attribute.isAutoIncremented();
        _iDatabaseSize     = attribute.getDatabaseSize() != null ? attribute.getDatabaseSize() : 0 ;
        _sDatabaseComment  = StrUtil.notNull( attribute.getDatabaseComment() ) ; // Added in v 2.1.1 - #LCH
        _sDatabaseDefaultValue = StrUtil.notNull( attribute.getDatabaseDefaultValue() ) ; 
        _bDatabaseNotNull  = attribute.isDatabaseNotNull();
        
		//--- Further info for ALL
        //_bNotNull   = column.getJavaNotNull();
        _bNotNull   = attribute.isNotNull();  // v 3.0.0
        _sLabel     = StrUtil.notNull( attribute.getLabel() ) ;
        _sInputType = StrUtil.notNull( attribute.getInputType() );
        
		//--- Further info for BOOLEAN 
//        _sBooleanTrueValue   = column.getBooleanTrueValue().trim() ;
//		_sBooleanFalseValue  = column.getBooleanFalseValue().trim() ;
        _sBooleanTrueValue   = Util.trim(attribute.getBooleanTrueValue(), VOID_STRING) ; 
		_sBooleanFalseValue  = Util.trim(attribute.getBooleanFalseValue(), VOID_STRING) ;
		
		//--- Further info for NUMBER 
	    //_sMinValue = Util.numberToString(attribute.getMinValue(), VOID_STRING ) ; // v 3.0.0
	    _sMinValue = Util.bigDecimalToString(attribute.getMinValue(), VOID_STRING ) ; // v 3.0.0
	    //_sMaxValue = Util.numberToString(attribute.getMaxValue(), VOID_STRING ) ; // v 3.0.0 
	    _sMaxValue = Util.bigDecimalToString(attribute.getMaxValue(), VOID_STRING ) ; // v 3.0.0 

		//--- Further info for STRING 
        //_bLongText  = column.getLongText() ;
        _bLongText  = attribute.isLongText() ; // v 3.0.0
        //_bNotEmpty  = column.getNotEmpty();
        _bNotEmpty  = attribute.isNotEmpty(); // v 3.0.0
        //_bNotBlank  = column.getNotBlank();
        _bNotBlank  = attribute.isNotBlank(); // v 3.0.0
        //_sMaxLength = column.getMaxLength();
        //_sMaxLength = Util.numberToString(attribute.getMaxLength(), VOID_STRING); // v 3.0.0
        _sMaxLength = Util.integerToString(attribute.getMaxLength(), VOID_STRING); // v 3.0.0
        //_sMinLength = column.getMinLength();
        //_sMinLength = Util.numberToString(attribute.getMinLength(), VOID_STRING); // v 3.0.0
        _sMinLength = Util.integerToString(attribute.getMinLength(), VOID_STRING); // v 3.0.0
        _sPattern   = StrUtil.notNull( attribute.getPattern() );
        
		//--- Further info for DATE/TIME 
//		if ( RepositoryConst.SPECIAL_DATE_ONLY.equalsIgnoreCase(column.getDateType()) ) {
//			_iDateType = DATE_ONLY;
//		} else if ( RepositoryConst.SPECIAL_TIME_ONLY.equalsIgnoreCase(column.getDateType()) )  {
//			_iDateType = TIME_ONLY;
//		} else if ( RepositoryConst.SPECIAL_DATE_AND_TIME.equalsIgnoreCase(column.getDateType()) )  {
//			_iDateType = DATE_AND_TIME;
//		} else {
//			_iDateType =  -1  ; // Default : UNKNOWN
//		}
		_dateType = ( attribute.getDateType() != null ?  attribute.getDateType() : DateType.UNDEFINED ); // v 3.0.0
		
        _bDatePast   = attribute.isDatePast();
        _bDateFuture = attribute.isDateFuture();
        _bDateBefore = attribute.isDateBefore();
        _sDateBeforeValue = StrUtil.notNull( attribute.getDateBeforeValue() );
        _bDateAfter  = attribute.isDateAfter();
        _sDateAfterValue  = StrUtil.notNull( attribute.getDateAfterValue() );
        
		//--- Further info for JPA         
        if ( attribute.isAutoIncremented() ) {
		    _bGeneratedValue = true ;
			_sGeneratedValueStrategy  = VOID_STRING ; // "AUTO" is the default strategy 
			_sGeneratedValueGenerator = VOID_STRING ;
        } 
        else {
			//if (column.getGeneratedValue() != null) {
        	if (attribute.isGeneratedValue() ) { // v 3.0.0
			    _bGeneratedValue = true ;
				//_sGeneratedValueStrategy  = column.getGeneratedValue().getStrategy();
				_sGeneratedValueStrategy  = StrUtil.notNull( attribute.getGeneratedValueStrategy() ); // v 3.0.0
				//_sGeneratedValueGenerator = column.getGeneratedValue().getGenerator();
				_sGeneratedValueGenerator = StrUtil.notNull( attribute.getGeneratedValueGenerator() ); // v 3.0.0
			}
			else {
				_bGeneratedValue = false;
				_sGeneratedValueStrategy  = VOID_STRING;
				_sGeneratedValueGenerator = VOID_STRING;
			}
        }
			        
		//if ( column.getTableGenerator() != null ) {
		if ( attribute.hasTableGenerator() ) { // v 3.0.0
		    _bTableGenerator = true ;
			//_sTableGeneratorName = column.getTableGenerator().getName();
			_sTableGeneratorName = StrUtil.notNull(attribute.getTableGeneratorName()); // v 3.0.0
			//_sTableGeneratorTable = column.getTableGenerator().getTable();
			_sTableGeneratorTable = StrUtil.notNull(attribute.getTableGeneratorTable()); // v 3.0.0
			//_sTableGeneratorPkColumnName = column.getTableGenerator().getPkColumnName();
			_sTableGeneratorPkColumnName = StrUtil.notNull(attribute.getTableGeneratorPkColumnName()); // v 3.0.0
			//_sTableGeneratorValueColumnName = column.getTableGenerator().getValueColumnName();
			_sTableGeneratorValueColumnName = StrUtil.notNull(attribute.getTableGeneratorValueColumnName()); // v 3.0.0
			//_sTableGeneratorPkColumnValue = column.getTableGenerator().getPkColumnValue();
			_sTableGeneratorPkColumnValue = StrUtil.notNull(attribute.getTableGeneratorPkColumnValue()); // v 3.0.0
		}
		else {
		    _bTableGenerator = false ;
			_sTableGeneratorName = VOID_STRING ;
			_sTableGeneratorTable = VOID_STRING ;
			_sTableGeneratorPkColumnName = VOID_STRING ;
			_sTableGeneratorValueColumnName = VOID_STRING;
			_sTableGeneratorPkColumnValue = VOID_STRING;
		}

		//if (column.getSequenceGenerator() != null) {
		if (attribute.hasSequenceGenerator() ) {
		    _bSequenceGenerator = true;
			//_sSequenceGeneratorName = column.getSequenceGenerator().getName();
			_sSequenceGeneratorName = attribute.getSequenceGeneratorName();
			//_sSequenceGeneratorSequenceName = column.getSequenceGenerator().getSequenceName();
			_sSequenceGeneratorSequenceName = attribute.getSequenceGeneratorSequenceName();
			//_iSequenceGeneratorAllocationSize = column.getSequenceGenerator().getAllocationSize();
			_iSequenceGeneratorAllocationSize = Util.intValue(attribute.getSequenceGeneratorAllocationSize(), 0);
		}
		else {
		    _bSequenceGenerator = false;
			_sSequenceGeneratorName = VOID_STRING;
			_sSequenceGeneratorSequenceName = VOID_STRING;
			_iSequenceGeneratorAllocationSize = -1;
		}
		
		
		_bIsUsedInLinks         = attribute.isUsedInLinks(); // v 3.0.0 #LGU
		_bIsUsedInSelectedLinks = attribute.isUsedInSelectedLinks(); // v 3.0.0 #LGU
	}
	
	protected final LanguageType getLanguageType() {
		// TypeConverter typeConverter = new TypeConverterForJava() ;
		TypeConverter typeConverter =_env.getTypeConverter();
		LanguageType languageType = typeConverter.getType(this.attributeTypeInfo);
		if ( languageType != null ) {
			return languageType ;
		}
		else {
			throw new IllegalStateException("Cannot get language type for '" + this._sNeutralType + "'");
		}
	}
	
	//-----------------------------------------------------------------------------------------------
	/* package */ void useFullType ()
	{
		_bUseFullType = true ;
	}
	
	@VelocityMethod(
			text={	
				"Returns the name of the attribute "
				}
		)
	public String getName()
	{
		return _sName;
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
			text={	
				"Returns the entity owning the attribute "
				}
		)
	public EntityInContext getEntity()
	{
		return _entity;
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the attribute's name with trailing blanks in order to obtain the expected size "
			},
		parameters = { 
			"n : the expected size" 
			}
	)
	public String formattedName(int iSize)
    {
//        String s = _sName ;
//        String sTrailingBlanks = "";
//        int iDelta = iSize - s.length();
//        if (iDelta > 0) // if needs trailing blanks
//        {
//            sTrailingBlanks = GeneratorUtil.blanks(iDelta);
//        }
//        return s + sTrailingBlanks;
        return format(this.getName(), iSize);
    }

	//-------------------------------------------------------------------------------------
	/**
	 * Returns the "neutral type" defined in the model <br>
	 * e.g. : "string", "short", "decimal", "boolean", "date", "time", etc <br>
	 * 
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the 'neutral type', that is to say the type as defined in the model",
			"e.g. : 'string', 'short', 'decimal', 'boolean', 'date', 'time', etc "
			}
	)
	public String getNeutralType() {
		return _sNeutralType ;
	}

	//-------------------------------------------------------------------------------------
	/**
	 * Returns the "java type" to use <br>
	 * usually the simple type ( "int", "BigDecimal", "Date" ) <br>
	 * sometimes the full type ( if the simple type is ambiguous )
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the recommended type for the attribute",
			"usually the simple type ( 'int', 'BigDecimal', 'Date' ) ",
			"sometimes the full type ( if the simple type is considered as ambiguous )",
			"Examples for Java : 'int', 'BigDecimal', 'Date', 'java.util.Date', 'java.sql.Date' "
			}
	)
	public String getType() 
	{
//		// return _sType;
//		// v 2.0.7
//		if ( _bUseFullType ) {
//			return _sFullType ;
//		}
//		else {
//			return _sSimpleType ;
//		}
		// v 3.0.0
		LanguageType type = getLanguageType();
		if ( _bUseFullType ) {
			return type.getFullType() ;
		}
		else {
			return type.getSimpleType() ;
		}
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the attribute's type with trailing blanks in order to obtain the expected size"
			},
		parameters = { 
			"n : the expected size " 
			}
	)
	public String formattedType(int iSize) 
    {
//		String sType = this.getType() ;
//        String sTrailingBlanks = "";
//        int iDelta = iSize - sType.length();
//        if (iDelta > 0) // if needs trailing blanks
//        {
//            sTrailingBlanks = GeneratorUtil.blanks(iDelta);
//        }
//        return sType + sTrailingBlanks;
        return format(this.getType(), iSize);
    }	
    
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the attribute's wrapper type with trailing blanks in order to obtain the expected size"
			},
		parameters = { 
			"n : the expected size " 
			}
	)
	public String formattedWrapperType(int iSize) 
    {
        return format(this.getWrapperType(), iSize);
    }	
    
	private String format(String s, int iSize) {
        String sTrailingBlanks = "";
        int iDelta = iSize - s.length();
        if (iDelta > 0) { // if trailing blanks needed
            sTrailingBlanks = GeneratorUtil.blanks(iDelta);
        }
        return s + sTrailingBlanks;
    }	

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
				"Returns the full type name",
				"e.g. for a Java object type : java.math.BigDecimal, java.util.Date,  .. ",
				"  or for a Java primitive type : short, int, .. "
			}
	)
	public String getFullType() 
	{
		//return _sFullType;
		// v 3.0.0
		LanguageType type = getLanguageType();
		return type.getFullType() ;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the simple type name ",
			"e.g. for a Java object type : BigDecimal, Date, Integer, .. ",
			"  or for a Java primitive type : short, int, .. "
			}
	)
	public String getSimpleType() 
	{
		//return _sSimpleType;
		// v 3.0.0
		LanguageType type = getLanguageType();
		return type.getSimpleType() ;
	}
	
	//-------------------------------------------------------------------------------------
	/**
	 * Returns the "java wrapper type" ie "Float" for "float" type, "Boolean" for "boolean" type
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the wrapper type corresponding to the attribute's primitive type",
			"Examples : 'Float' for 'float', 'Integer' for 'int', 'Boolean' for 'boolean', ... ",
			"The attribute's type is retuned as is if it's not a primitive type"
			}
	)
	public String getWrapperType()
	{
//		if ( null == _sSimpleType ) return "UnknownType" ;
//		
//		final String t = _sSimpleType;
//		if ("byte".equals(t)) {
//			return "Byte";
//		} else if ("short".equals(t)) {
//			return "Short";
//		} else if ("int".equals(t)) {
//			return "Integer";
//		} else if ("long".equals(t)) {
//			return "Long";
//		} else if ("float".equals(t)) {
//			return "Float";
//		} else if ("double".equals(t)) {
//			return "Double";
//		} else if ("boolean".equals(t)) {
//			return "Boolean";
//		} else if ("char".equals(t)) {
//			return "Character";
//		} else {
//			return t;
//		}
		// v 3.0.0
		LanguageType type = getLanguageType();
		return type.getWrapperType() ;		
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the type of the date : $const.DATE_ONLY, $const.TIME_ONLY, $const.DATE_AND_TIME"
			}
	)
	public int getDateType()
	{
		//return _iDateType ;
		return _dateType.getValue(); // returns the enum value
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if there's an initial value for the attribute"
			}
	)
	public boolean hasInitialValue()
	{
//		return _sInitialValue != null ;
//		return _sInitialValue != null && ( "".equals(_sInitialValue) == false ) ;
		return ! StrUtil.nullOrVoid(_sInitialValue); // v 3.0.0
	}
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the initial value for the attribute"
			}
	)
	public String getInitialValue()
	{
		return _sInitialValue;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
				"Returns the getter for the attribute",
				"e.g : 'getFoo' for 'foo' (or 'isFoo' for a boolean primitive type)"
					}
	)
	public String getGetter() 
	{
		return Util.buildGetter(_sName, this.getType() ); // v 2.0.7
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
				"Returns the getter for the attribute with always a 'get' prefix",
				"even for a boolean"
					}
	)
	public String getGetterWithGetPrefix()
	{
		return Util.buildGetter(_sName); // v 2.0.7
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
				"Returns the setter for the attribute",
				"e.g : 'setFoo' for 'foo' "
				}
	)
	public String getSetter()
	{
		return Util.buildSetter(_sName);
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the value to use for a boolean when is TRUE (eg to be stored in a database) "
			}
	)
	public String getBooleanTrueValue()
	{
		return _sBooleanTrueValue;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the value to use for a boolean when is FALSE (eg to be stored in a database) "
			}
	)
	public String getBooleanFalseValue()
	{
		return _sBooleanFalseValue ;
	}
	
	//----------------------------------------------------------------------
	// Database 
	//----------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the database name for the attribute",
			"Typically the column name for a relational database"
			}
	)
    public String getDatabaseName()
    {
        return _sDataBaseName;
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the database native type for the attribute",
			"For example : INTEGER, VARCHAR, etc..."
			}
	)
    public String getDatabaseType()
    {
        return _sDataBaseType;
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the database native type for the attribute with the size if it makes sens",
			"For example : INTEGER, VARCHAR(24), NUMBER, CHAR(3), etc..."
			},
		since="2.0.7"
	)
    public String getDatabaseTypeWithSize()
    {
        return DatabaseUtil.getNativeTypeWithSize(_sDataBaseType, _iDatabaseSize, _iJdbcTypeCode);
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the database size for the attribute"
			}
	)
    public int getDatabaseSize()
    {
        return _iDatabaseSize ;
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the database comment for the attribute"
			},
		since="2.1.1"
	)
    public String getDatabaseComment()
    {
        return _sDatabaseComment;
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute has a database default value"
			}
	)
    public boolean hasDatabaseDefaultValue()
    {
    	if ( _bAutoIncremented ) return false ; // No default value for auto-incremented fields
        if ( _sDatabaseDefaultValue != null )
        {
        	if ( _sDatabaseDefaultValue.length() > 0 ) return true ;
        }
        return false ;
    }
    
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns the database default value for the attribute (or a void string if none)"
		}
	)
    public String getDatabaseDefaultValue()
    {
    	if ( hasDatabaseDefaultValue() ) return _sDatabaseDefaultValue ;
        return "" ;
    }
    
	//----------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute must be NOT NULL when stored in the database"
		}
	)
    public boolean isDatabaseNotNull()
    {
        return _bDatabaseNotNull;
    }
    
	//----------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the JDBC type of the attribute (the type code)"
			}
		)
    public int getJdbcTypeCode()
    {
        return _iJdbcTypeCode ;
    }

	//----------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the JDBC type name ('CHAR', 'VARCHAR', 'NUMERIC', ... )<br>",
			"The 'java.sql.Types' constant name for the current JDBC type code"
			}
		)
    public String getJdbcTypeName()
    {
        return _sJdbcTypeName ;
    }

	//----------------------------------------------------------------------
    /**
     * Returns the recommended Java type for the JDBC type 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the recommended Java type for the JDBC type of the attribute"
				}
		)
    public String getJdbcRecommendedJavaType()
    {
    	JdbcTypes types = JdbcTypesManager.getJdbcTypes();
    	return types.getJavaTypeForCode(_iJdbcTypeCode, _bDatabaseNotNull );
    }

	//----------------------------------------------------------------------
    /**
     * Returns TRUE if the attribute is a Database Primary Key element
     * @return 
     */
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute is the Primary Key or a part of the Primary Key in the database"
		}
	)
    public boolean isKeyElement()
    {
        return _bKeyElement;
    }

	//----------------------------------------------------------------------
	@VelocityMethod(
	text={ "Returns TRUE if the attribute is used in (at least) one Foreign Key",
		"( it can be an 'Simple FK' or a 'Composite FK' or both )" },
	since="3.0.0"
	)
//    public boolean isUsedInForeignKey() {
//        return _bUsedInForeignKey ;
//    }
    public boolean isFK() { // v 3.0.0
        return _bForeignKey ;
    }

	//----------------------------------------------------------------------
	@VelocityMethod(
	text={ "Returns TRUE if the attribute is itself a 'Simple Foreign Key' ",
		   "( the FK is based only on this single attribute ) " },
	since="3.0.0"
	)
    public boolean isFKSimple() { // v 3.0.0
        return _bForeignKeySimple ;
    }

	//----------------------------------------------------------------------
	@VelocityMethod(
	text={ "Returns TRUE if the attribute is a part of a 'Composite Foreign Key' ",
		   "( the FK is based on many attributes including this attribute ) " },
	since="3.0.0"
	)
    public boolean isFKComposite() { // v 3.0.0
        return _bForeignKeyComposite ;
    }

	//----------------------------------------------------------------------
//	/**
//     * Returns TRUE if the attribute is involved in a link Foreign Key <br>
//     * Useful for JPA, to avoid double mapping ( FK field and owning side link )
//     * @param linksArray - list of the links to be checked 
//     * @return
//     */
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute is involved in a link Foreign Key",
//		"Useful for JPA, to avoid double mapping ( FK field and owning side link )"
//		},
//	parameters="links : list of links where to search the attribute"
//	)
//    public boolean isUsedInLinkJoinColumn( List<LinkInContext> links )
//    {
//    	if ( null == _sDataBaseName ) {
//    		return false ; // No mapping 
//    	}
//    	
//		for ( LinkInContext link : links ) {
//			if( link.isOwningSide() ) {
//				if ( link.usesAttribute(this) ) {
//					return true ;
//				}					
//			}
//		}
//		return false ;
//    }
	
	public boolean isUsedInLinks() { // v 3.0.0 #LGU
		return _bIsUsedInLinks ;
	}
	public boolean isUsedInSelectedLinks() { // v 3.0.0 #LGU
		return _bIsUsedInSelectedLinks ;
	}
	
	// Implementation in each MODEL :
//	public boolean isUsedInLinks_DBREP() {
//		return isUsedInLink_DBREP( this.getEntity().getLinks() );
//	}
//	public boolean isUsedInSelectedLinks_DBREP() {
//		return isUsedInLink_DBREP( this.getEntity().getSelectedLinks() );
//	}
//
//	public boolean isUsedInLinks_DSL() {
//		// In this model no "selected link" vs "all links" => don't care the given links list
//		return this.isFK() ;
//	}
//	public boolean isUsedInSelectedLinks_DSL() {
//		// In this model no "selected link" vs "all links" => don't care the given links list
//		return this.isFK() ;
//	}
//	private boolean isUsedInLink_DBREP( List<LinkInContext> links ) // SAME AS BEFORE
//	{
////		if ( null == this._sDataBaseName ) {
////			return false ; // No mapping for this attribute
////	    }
//		if ( links != null ) {
//			for ( LinkInContext link : links ) {
//				if( link.isOwningSide() ) {
//					if ( link.usesAttribute(this) ) {
//						return true ;
//					}
//				}
//			}
//		}
//		return false ;
//	}

	//-------------------------------------------------------------------------------------
    /**
     * Returns TRUE if the attribute is auto-incremented by the Database engine
     * @return 
     */
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute is 'auto-incremented' by the database",
		"when a new entity is inserted in the database"
		}
	)
    public boolean isAutoIncremented()
    {
        return _bAutoIncremented;
    }

	//----------------------------------------------------------------------
    /**
     * Returns TRUE if the attribute has a "Not Null" constraint at the Java level
     * @return 
     */
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute has a 'Not Null' validation rule "
		}
	)
    public boolean isNotNull()
    {
        return _bNotNull;
    }

	//----------------------------------------------------------------------
    /**
     * Returns the label defined for the attribute 
     * @since v 2.0.3
     * @return
     */
	@VelocityMethod(
		text={	
			"Returns the label for the attribute "
			}
	)
    public String getLabel()
    {
        return _sLabel ;
    }
    
	//----------------------------------------------------------------------
    /**
     * Returns the "input type" defined for this attribute 
     * @since v 2.0.3
     * @return
     */
	@VelocityMethod(
		text={	
			"Returns the 'input type' defined for the attribute",
			"Typically for HTML 5 : 'number', 'date', ..."
			},
		since="2.0.3"
	)
    public String getInputType()
    {
        return _sInputType ;
    }
    
	//-------------------------------------------------------------------------------------
/*****
	@VelocityMethod(
		text={	
			"Returns maximum input length to be used in the GUI ",
			"For string types the specific maximum lenght is returned ( or void if not defined )",
			"For numeric types the maximum lenght depends on the type ( 4 for 'byte', 11 for 'int', etc... ) ",
			"For 'date' 10, for 'time' 8"
			}
	)
    public String getGuiMaxLength() 
    {
//		String t = _sSimpleType ;
//    	//--- Max length depending on the Java type
//    	if ( "byte".equals(t)  || "Byte".equals(t)    ) return  "4" ; // -128 to +127
//    	if ( "short".equals(t) || "Short".equals(t)   ) return  "6" ; // -32768 to +32767
//    	if ( "int".equals(t)   || "Integer".equals(t) ) return "11" ; // -2147483648 to +2147483647
//    	if ( "long".equals(t)  || "Long".equals(t)    ) return "20" ; // -9223372036854775808 to +9223372036854775807
//    	
//    	if ( "double".equals(t) || "Double".equals(t) ) return "20" ; // Arbitrary fixed value like long
//    	if ( "float".equals(t)  || "Float".equals(t)  ) return "20" ; // Arbitrary fixed value like long
//    	
//    	if ( "BigDecimal".equals(t) ) return "20" ; // Arbitrary fixed value like long
//    	if ( "BigInteger".equals(t) ) return "20" ; // Arbitrary fixed value like long
//    	
//    	if ( "Date".equals(t) ) return "10" ; // "YYYY-MM-DD", "DD/MM/YYYY", etc ...
//    	if ( "Time".equals(t) ) return "8" ; // "HH:MM:SS"
//
//    	//--- Max length from Database column size (only for String)
//    	if ( "String".equals(t) )
//    	{
//    		return voidIfNull ( _sMaxLength ) ;
//    	}
//		return "";
    }
****/
	
//    /**
//     * Shortcut for Velocity attribute syntax : $var.guiMaxLengthAttribute 
//     * @return
//     */
//	//-------------------------------------------------------------------------------------
//	@VelocityMethod(
//		text={	
//			"Returns the GUI 'maxlength' attribute (or void if none) ",
//			"e.g 'maxlength=12' "
//			}
//	)
//    public String getGuiMaxLengthAttribute() 
//    {
//    	// return guiMaxLengthAttribute() ;
//    }
    
//    //-------------------------------------------------------------------------------------------
//    /**
//     * For Velocity function call syntax : $var.guiMaxLengthAttribute() 
//     * @return
//     */
//	//-------------------------------------------------------------------------------------
//	@VelocityMethod(
//		text={	
//			"Returns the GUI 'maxlength' attribute (or void if none) ",
//			"e.g 'maxlength=12' "
//			}
//	)
//    public String guiMaxLengthAttribute() 
//    {
//    	return guiMaxLengthAttribute("maxlength") ;
//    }
    
//    /**
//     * For Velocity function call syntax : $var.guiMaxLengthAttribute('maxlength') 
//     * @param attributeName
//     * @return
//     */
//	//-------------------------------------------------------------------------------------
//	@VelocityMethod(
//		text={	
//			"Returns the GUI specific attribute for maximum length (or void if none) ",
//			"e.g 'myattribute=12' for guiMaxLengthAttribute('myattribute') "
//			},
//		parameters = "guiAttributeName : the name of the attribute to be set in the GUI"
//	)
//    public String guiMaxLengthAttribute(String attributeName) 
//    {
//    	if ( attributeName != null )
//    	{
//    		String s = getGuiMaxLength();
//    		if ( ! StrUtil.nullOrVoid(s) )
//    		{
//    			return attributeName + "=\"" + s + "\"" ;
//    		}
//    	}
//    	return "";
//    }
    
    //-------------------------------------------------------------------------------------------
    /**
     * Returns the "maximum" length if any, else returns "" 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the maximum length for the attribute (if any, else returns void) "
				}
		)
    public String getMaxLength() 
    {
    	return voidIfNull(_sMaxLength) ;
    }
    /**
     * Returns the "minimum" length if any, else returns "" 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the minimum length for the attribute (if any, else returns void) "
				}
		)
    public String getMinLength() 
    {
    	return voidIfNull(_sMinLength) ;
    }
    
    //-------------------------------------------------------------------------------------------
    /**
     * Returns the "pattern" (Reg Exp) if any, else returns "" 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the Reg Exp pattern defined for the attribute (if any, else returns void) "
				}
		)
    public String getPattern() 
    {
    	return voidIfNull(_sPattern) ;
    }
    
    //-------------------------------------------------------------------------------------------
    /**
     * Returns the "minimum" value if any, else returns "" 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the minimum value for the attribute (if any, else returns void) "
				}
		)
    public String getMinValue() 
    {
    	return voidIfNull(_sMinValue) ;
    }
    
    //-------------------------------------------------------------------------------------------
    /**
     * Returns the "maximum" value if any, else returns "" 
     * @return
     */
	@VelocityMethod(
			text={	
				"Returns the maximum value for the attribute (if any, else returns void) "
				}
		)
    public String getMaxValue() 
    {
    	return voidIfNull(_sMaxValue) ;
    }
    //-------------------------------------------------------------------------------------------
//    /**
//     * Synonym for Velocity attribute syntax : $var.guiMinMaxAttributes 
//     * @return
//     */
//	@VelocityMethod(
//		text={	
//			"Returns the GUI attributes for minimum and maximum values (or void if none)",
//			"e.g 'min=10 max=20' "
//			}
//	)
//    public String getGuiMinMaxAttributes() 
//    {
//    	//return guiMinMaxAttributes() ;
//    }
    
//	//-------------------------------------------------------------------------------------
//    /**
//     * For Velocity function call syntax : $var.guiMinMaxAttributes() 
//     * @return
//     */
//	@VelocityMethod(
//		text={	
//			"Returns the GUI attributes for minimum and maximum values (or void if none)",
//			"e.g 'min=10 max=20' "
//			}
//	)
//    public String guiMinMaxAttributes() 
//    {
//    	return guiMinMaxAttributes("min", "max") ;
//    }

//    //-------------------------------------------------------------------------------------------
//    /**
//     * For Velocity function call syntax : $var.guiMinMaxAttributes('min','max') 
//     * @param attributeName
//     * @return
//     */
//	@VelocityMethod(
//		text={	
//			"Returns the GUI specific attribute for minimum and maximum values (or void if none) ",
//			"e.g 'mini=10 maxi=20' for guiMaxLengthAttribute('mini', 'maxi') "
//			},
//		parameters = {
//			"guiMinAttributeName : the name of the MIN attribute to be set in the GUI",
//			"guiMaxAttributeName : the name of the MAX attribute to be set in the GUI"
//		}
//	)
//    public String guiMinMaxAttributes(String minAttributeName, String maxAttributeName  ) 
//    {
//    	if ( minAttributeName != null && maxAttributeName != null )
//    	{
//    		String sMin = getMinValue();
//    		String sMinAttr = "" ;
//    		if ( ! StrUtil.nullOrVoid(sMin) )
//    		{
//    			sMinAttr = minAttributeName + "=\"" + sMin + "\"" ;
//    		}
//    		
//    		String sMax = getMaxValue();
//    		String sMaxAttr = "" ;
//    		if ( ! StrUtil.nullOrVoid(sMax) )
//    		{
//    			sMaxAttr = maxAttributeName + "=\"" + sMax + "\"" ;
//    		}
//    		return sMinAttr + " " + sMaxAttr ;
//    	}
//    	return "" ;
//    }

//    /**
//     * Returns the GUI "type" if any, else returns "" 
//     * @return
//     */
//	@VelocityMethod(
//			text={	
//				"Returns the GUI type if any (else returns a void string)",
//				"e.g 'int', 'num', 'date', 'time', '' "
//				}
//		)
//    public String getGuiType() 
//    {
////		String t = _sSimpleType ; // v 2.0.7
////    	//--- type="int"
////    	if ( "byte".equals(t)  || "Byte".equals(t)    ) return TYPE_INT ;
////    	if ( "short".equals(t) || "Short".equals(t)   ) return TYPE_INT ; 
////    	if ( "int".equals(t)   || "Integer".equals(t) ) return TYPE_INT ; 
////    	if ( "long".equals(t)  || "Long".equals(t) )    return TYPE_INT ; 
////    	if ( "BigInteger".equals(t) )   return TYPE_INT ;
////
////    	//--- type="num"
////    	if ( "float".equals(t)  || "Float".equals(t) )    return TYPE_NUM ; 
////    	if ( "double".equals(t) || "Double".equals(t) )   return TYPE_NUM ; 
////    	if ( "BigDecimal".equals(t) )   return TYPE_NUM ;
////    	
////    	//--- type="date"
////    	if ( "Date".equals(t) )   return TYPE_DATE ;
////    	
////    	//--- type="time"
////    	if ( "Time".equals(t) )   return TYPE_TIME ;
////    	
////    	return "" ;
//    }
    
//    /**
//     * For Velocity attribute syntax : $var.guiTypeAttribute
//     * @return
//     */
//	@VelocityMethod(
//			text={	
//				"Returns the GUI type attribute ",
//				"e.g : type='int' "
//				}
//				)
//    public String getGuiTypeAttribute() 
//    {
//    	return guiTypeAttribute() ;
//    }
//    /**
//     * For Velocity function call syntax : $var.guiTypeAttribute() 
//     * @return
//     */
//	@VelocityMethod(
//			text={	
//				"Returns the GUI type attribute ",
//				"e.g : type='int' "
//				}
//				)
//    public String guiTypeAttribute() 
//    {
//    	return guiTypeAttribute("type") ;
//    }
//    /**
//     * For Velocity function call syntax : $var.guiTypeAttribute('type') 
//     * @param attributeName
//     * @return
//     */
//	@VelocityMethod(
//	text={	
//		"Returns the GUI type attribute ",
//		"e.g : type='int' "
//		},
//	parameters={
//			"guiTypeAttributeName : name of the TYPE attribute to be set in the GUI "
//		}
//		)
//    public String guiTypeAttribute(String attributeName) 
//    {
//    	if ( attributeName != null )
//    	{
//    		String s = getGuiType();
//    		if ( ! StrUtil.nullOrVoid(s) )
//    		{
//    			return attributeName + "=\"" + s + "\"" ;
//    		}
//    	}
//    	return "";
//    }
    
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute must be validated as a date in the past"
			}
	)
	public boolean hasDatePastValidation() {
		return _bDatePast;
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute must be validated as a date in the future"
			}
	)
	public boolean hasDateFutureValidation() {
		return _bDateFuture;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute must be validated as a date before a given date value"
			}
	)
	public boolean hasDateBeforeValidation() {
		return _bDateBefore;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the 'date before' value (for date validation)"
			}
	)
	public String getDateBeforeValue() {
		return _sDateBeforeValue;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute must be validated as a date after a given date value"
			}
	)
	public boolean hasDateAfterValidation() {
		return _bDateAfter;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the 'date after' value (for date validation)"
			}
	)
	public String getDateAfterValue() {
		return _sDateAfterValue;
	}

	//-----------------------------------------------------------------------------
    
    /**
     * Returns true if the attribute is a long text
     * @return 
     */
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute is a 'Long Text' ",
		"i.e. that cannot be transported in a classical string",
		"Typically a text stored as a CLOB or a BLOB"
		}
	)
    public boolean isLongText()
    {
        return _bLongText;
    }

	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute has a 'Not Empty' validation rule "
		}
	)
    public boolean isNotEmpty()
    {
        return _bNotEmpty;
    }
    
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute has a 'Not Blank' validation rule "
		}
	)
    public boolean isNotBlank()
    {
        return _bNotBlank;
    }
    
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if there's a default value for the attribute"
			}
	)
    public boolean hasDefaultValue() // Velocity : $attrib.hasDefaultValue()
    {
    	return ! StrUtil.nullOrVoid(_sDefaultValue) ;
    }
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the default value for the attribute"
			}
	)
    public String getDefaultValue() // Velocity : ${attrib.defaultValue}
    {
    	return _sDefaultValue ;
    }

	public String toString() 
	{
		String s =  _sInitialValue != null ? " = " + _sInitialValue : "" ;
		return this.getType() + " " + _sName + s ; 
	}

	private String voidIfNull ( String s ) {
		return s != null ? s : "" ;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute is selected (ckeckbox ckecked in the GUI)"
			}
	)
	public boolean isSelected() {
		return _bSelected;
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute is insertable (ckeckbox ckecked in the GUI)"
			}
	)
	public boolean isInsertable() {
		return _bInsertable;
	}
		
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute is insertable (ckeckbox ckecked in the GUI)"
			}
	)
	public boolean isUpdatable() {
		return _bUpdatable;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's language type is a primitive type",
		"i.e. for Java : int, float, boolean, ..."
		}
	)
	public boolean isPrimitiveType()
	{
//		return JavaTypeUtil.isPrimitiveType( _sSimpleType );
		// v 3.0.0
		LanguageType type = getLanguageType();
		return type.isPrimitiveType() ;		
	}

//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is a Java array ( byte[], String[], ... )"
//		},
//	since="2.0.7"
//	)
//	public boolean isArrayType()
//	{
//		String s = _sSimpleType ;
//		if ( s != null ) {
//			if ( s.trim().endsWith("]")) {
//				return true ;
//			}
//		}
//    	return false ;
//	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is'boolean' "
		},
	since="2.0.7"
	)
	public boolean isBooleanType()
	{
//    	if ( "boolean".equals(_sSimpleType) )   return true ;
//    	if ( "Boolean".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.BOOLEAN.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'byte' "
		},
	since="2.0.7"
	)
	public boolean isByteType()
	{
//    	if ( "byte".equals(_sSimpleType) )   return true ;
//    	if ( "Byte".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.BYTE.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'short' "
		},
	since="2.0.7"
	)
	public boolean isShortType()
	{
//    	if ( "short".equals(_sSimpleType) )   return true ;
//    	if ( "Short".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.SHORT.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'int' "
		},
	since="2.0.7"
	)
	public boolean isIntegerType()
	{
//    	if ( "int".equals(_sSimpleType) )   return true ;
//    	if ( "Integer".equals(_sSimpleType) ) return true ;
    	if ( NeutralType.INTEGER.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'long' "
		},
	since="2.0.7"
	)
	public boolean isLongType()
	{
//    	if ( "long".equals(_sSimpleType) )   return true ;
//    	if ( "Long".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.LONG.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'float' "
		},
	since="2.0.7"
	)
	public boolean isFloatType()
	{
//    	if ( "float".equals(_sSimpleType) )   return true ;
//    	if ( "Float".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.FLOAT.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}
	
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'double' "
		},
	since="2.0.7"
	)
	public boolean isDoubleType()
	{
//    	if ( "double".equals(_sSimpleType) )   return true ;
//    	if ( "Double".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.DOUBLE.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'decimal' "
		},
//	since="2.0.7"
	since="3.0.0"
	)
//	public boolean isBigDecimalType()
	public boolean isDecimalType()
	{
//    	if ( "BigDecimal".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.DECIMAL.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}


	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is a number type",
		"( byte, short, int, long, decimal, float, double )"
		},
	since="2.0.7"
	)
	public boolean isNumberType()
	{
    	if ( isByteType() || isShortType() || isIntegerType() || isLongType() )   return true ;
    	if ( isFloatType() || isDoubleType() )   return true ;
//    	if ( isBigDecimalType() )   return true ;
    	if ( isDecimalType() )   return true ;
    	return false ;
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'string' "
		},
	since="2.0.7"
	)
	public boolean isStringType()
	{
//    	if ( "String".equals(_sSimpleType) )   return true ;
    	if ( NeutralType.STRING.equals(this._sNeutralType ) ) return true ; // v 3.0.0
    	return false ;
	}

//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is 'java.util.Date' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isUtilDateType()
//	{
//    	if ( "java.util.Date".equals(_sFullType) ) return true ;
//    	return false ;
//	}
//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is 'java.sql.Date' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isSqlDateType()
//	{
//    	if ( "java.sql.Date".equals(_sFullType) ) return true ;
//    	return false ;
//	}
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'date' "
		},
	since="3.0.0"
	)
	public boolean isDateType()
	{
		return NeutralType.DATE.equals(this._sNeutralType ) ; // v 3.0.0
	}
//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is 'java.sql.Time' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isSqlTimeType()
//	{
//    	if ( "java.sql.Time".equals(_sFullType) ) return true ;
//    	return false ;
//	}
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is 'time' "
		},
	since="3.0.0"
	)
	public boolean isTimeType() {
		return NeutralType.TIME.equals(this._sNeutralType ) ; // v 3.0.0
	}
//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is 'java.sql.Timestamp' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isSqlTimestampType()
//	{
//    	if ( "java.sql.Timestamp".equals(_sFullType) ) return true ;
//    	return false ;
//	}
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'timestamp' "
		},
	since="3.0.0"
	)
	public boolean isTimestampType() {
		return NeutralType.TIMESTAMP.equals(this._sNeutralType ) ; // v 3.0.0
	}
	
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's neutral type is a temporal type",
		"( date, time, timestamp )"
		},
	since="2.0.7"
	)
	public boolean isTemporalType()
	{
//    	if ( isUtilDateType() || isSqlDateType() || isSqlTimeType() || isSqlTimestampType() ) return true ;
    	if ( isDateType() || isTimeType() || isTimestampType() ) return true ;
    	return false ;
	}

//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is a Java 'Blob' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isBlobType()
//	{
//    	if ( "Blob".equals(_sSimpleType) )   return true ;
//    	return false ;
//	}
//
//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute's type is a Java 'Clob' type"
//		},
//	since="2.0.7"
//	)
//	public boolean isClobType()
//	{
//    	if ( "Clob".equals(_sSimpleType) )   return true ;
//    	return false ;
//	}
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
			"Returns TRUE if the attribute's neutral type is 'binary' "
		},
	since="3.0.0"
	)
	public boolean isBinaryType() {
		return NeutralType.BINARY.equals(this._sNeutralType )  ; // v 3.0.0
	}

	//-----------------------------------------------------------------------------------------
	// JPA "@GeneratedValue"
	//-----------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns TRUE if the attribute's value is generated when a new entity is inserted in the database",
		"It can be generated by the database ('auto-incremented') ",
		"or generated by the persistence layer (typically by JPA)"
		}
	)
	public boolean isGeneratedValue() {
		return _bGeneratedValue;
	}

	/**
	 * Returns the GeneratedValue strategy : auto, identity, sequence, table
	 * or null if not defined
	 * @return
	 */
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the strategy for a 'generated value' (or null if none)",
			"e.g : 'auto', 'identity', 'sequence', 'table' "
			}
	)
	public String getGeneratedValueStrategy() {
		return _sGeneratedValueStrategy;
	}

	//-------------------------------------------------------------------------------------
	/**
	 * Returns the GeneratedValue generator : the name of the primary key generator to use <br>
	 * The generator name referenced a "SequenceGenerator" or a "TableGenerator"
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the generator for a 'generated value' ",
			"Typically for JPA : 'SequenceGenerator' or 'TableGenerator' "
			}
	)
	public String getGeneratedValueGenerator() {
		return _sGeneratedValueGenerator;
	}

	//-----------------------------------------------------------------------------------------
	// JPA "@SequenceGenerator"
	//-----------------------------------------------------------------------------------------
	/**
	 * Returns true if this attribute is a "GeneratedValue" using a "SequenceGenerator"
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute is a 'generated value' using a 'sequence generator' ",
			"Typically for JPA '@SequenceGenerator'  "
			}
	)
	public boolean hasSequenceGenerator() {
		return _bSequenceGenerator;
	}

	//-----------------------------------------------------------------------------------------
	/**
	 * Returns the "@SequenceGenerator" name
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the name of the 'sequence generator' ",
			"Typically for JPA '@SequenceGenerator/name'  "
			}
	)
	public String getSequenceGeneratorName() {
		return _sSequenceGeneratorName;
	}

	//-----------------------------------------------------------------------------------------
	/**
	 * Returns the "@SequenceGenerator" sequence name
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the 'sequence name' to be used in the 'sequence generator' definition",
			"Typically for JPA '@SequenceGenerator/sequenceName'  "
			}
	)
	public String getSequenceGeneratorSequenceName() {
		return _sSequenceGeneratorSequenceName;
	}

	//-----------------------------------------------------------------------------------------
	/**
	 * Returns the "@SequenceGenerator" sequence allocation size
	 * @return
	 */
	@VelocityMethod(
		text={	
			"Returns the 'sequence allocation size' to be used in the 'sequence generator' definition",
			"Typically for JPA '@SequenceGenerator/allocationSize'  "
			}
	)
	public int getSequenceGeneratorAllocationSize() {
		return _iSequenceGeneratorAllocationSize;
	}

	//-----------------------------------------------------------------------------------------
	// JPA "@TableGenerator"
	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns TRUE if the attribute is a 'generated value' using a 'table generator' ",
			"Typically for JPA '@TableGenerator'  "
			}
	)
	public boolean hasTableGenerator() {
		return _bTableGenerator;
	}

	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the name of the 'table generator' ",
			"Typically for JPA '@TableGenerator/name'  "
			}
	)
	public String getTableGeneratorName() {
		return _sTableGeneratorName;
	}

	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the name of the table used in the 'table generator' ",
			"Typically for JPA '@TableGenerator/table'  "
			}
	)
	public String getTableGeneratorTable() {
		return _sTableGeneratorTable;
	}

	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the name of the Primary Key column used in the 'table generator' ",
			"Typically for JPA '@TableGenerator/pkColumnName'  "
			}
	)
	public String getTableGeneratorPkColumnName() {
		return _sTableGeneratorPkColumnName;
	}

	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the name of the column that stores the last value generated by the 'table generator' ",
			"Typically for JPA '@TableGenerator/valueColumnName'  "
			}
	)
	public String getTableGeneratorValueColumnName() {
		return _sTableGeneratorValueColumnName;
	}

	//-----------------------------------------------------------------------------------------
	@VelocityMethod(
	text={
		"Returns the primary key value in the generator table that distinguishes this set of generated values",
		"from others that may be stored in the table",
		"Typically for JPA '@TableGenerator/pkColumnValue'  "
		}
	)
	public String getTableGeneratorPkColumnValue() {
		return _sTableGeneratorPkColumnValue;
	}
	
// Removed in v 3.0.0
//	//------------------------------------------------------------------------------------------
//	@VelocityMethod(
//	text={	
//		"Returns the 'simple type' of the entity referenced by this attribute (if any) ",
//		"Returns a type only if the attribute is the only 'join column' of the link",
//		"else returns a 'void string' (if the attribute is not involved in a link, ",
//		"or if the link as many join columns)"
//		},
//	since="2.1.0"
//	)
//	public String getReferencedEntityType() throws GeneratorException {
//		for( LinkInContext link : _entity.getLinks()  ) {
//			if( link.isOwningSide() && link.getAttributesCount() == 1 ) {
//				if ( link.usesAttribute(this) ) {
//					return link.getTargetEntitySimpleType() ;
//				}					
//			}
//		}
//		return "";
//	}
	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns the entity referenced by this attribute (if any) ",
		"Can be used only if the attribute 'isFK' ",
		"Throws an exception if no entity is refrerenced by the attribute",
		},
	since="3.0.0"
	)
	public EntityInContext getReferencedEntity() throws GeneratorException {
		if ( ! StrUtil.nullOrVoid(_sReferencedEntityClassName) ) {
			EntityInContext entity = this._modelInContext.getEntityByClassName(_sReferencedEntityClassName);
			if ( entity != null ) {
				return entity ;
			}
			else {
				throw new IllegalStateException("getReferencedEntityType() : Cannot get Entity for '" + _sReferencedEntityClassName + "'");				
			}
		}
		else {
			throw new GeneratorException("No entity referenced by this attribute (" + _sName + ")");
		}
	}

	//------------------------------------------------------------------------------------------
	@VelocityMethod(
	text={	
		"Returns the name (class name) of the entity referenced by this attribute (if any) ",
		"Can be used only if the attribute 'isFK' ",
		"Throws an exception if no entity is refrerenced by the attribute",
		},
	since="3.0.0"
	)
	public String getReferencedEntityName() throws GeneratorException {
		return getReferencedEntity().getName();
	}

	//------------------------------------------------------------------------------------------
// REMOVED in v 3.0.0
//	@VelocityMethod(
//	text={	
//		"Returns TRUE if the attribute is referencing another entity by itself ",
//		"(if the attribute is the only 'join column' of a link)"
//		},
//		since="2.1.0"
//	)
//	public boolean isReferencingAnotherEntity() throws GeneratorException {
//		return getReferencedEntityType().length() > 0 ;
//	}
}