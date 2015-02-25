package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;

public class MindTouchConnectorComparator extends StringComparator
{
    private static final long serialVersionUID = -5421467841056660688L;

    // types of comparison
    public static final int ID = 0;
    public static final int NAME = 1;
    public static final int DESC = 2;
    public static final int URL = 3;
    public static final int USER_NAME = 4;
    public static final int COMPANY_NAME = 5;

    public MindTouchConnectorComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two MindTouchConnectorComparator objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        MindTouchConnector a = (MindTouchConnector) p_A;
        MindTouchConnector b = (MindTouchConnector) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case ID:
            rv = (int)(a.getId() - b.getId());
            break;
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case DESC:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;
        case URL:
            aValue = a.getUrl();
            bValue = b.getUrl();
            rv = this.compareStrings(aValue, bValue);
            break;
        case USER_NAME:
            aValue = a.getUsername();
            bValue = b.getUsername();
            rv = this.compareStrings(aValue, bValue);
            break;
        case COMPANY_NAME:
            aValue = a.getCompanyName();
            bValue = b.getCompanyName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
