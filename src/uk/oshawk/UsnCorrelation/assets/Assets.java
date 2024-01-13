package uk.oshawk.UsnCorrelation.assets;

import java.awt.Image;
import javax.swing.ImageIcon;
import uk.oshawk.UsnCorrelation.UsnUtil;

public class Assets {
    // Loads image assets used by the graphical user interface.
    
    public static final ImageIcon DEFAULT_DATA_OVERWRITE = new ImageIcon(new ImageIcon(Assets.class.getResource("default_data_overwrite.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon DEFAULT_DATA_EXTEND = new ImageIcon(new ImageIcon(Assets.class.getResource("default_data_extend.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon DEFAULT_DATA_TRUNCATE = new ImageIcon(new ImageIcon(Assets.class.getResource("default_data_truncate.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon NAMED_DATA_OVERWRITE = new ImageIcon(new ImageIcon(Assets.class.getResource("named_data_overwrite.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon NAMED_DATA_EXTEND = new ImageIcon(new ImageIcon(Assets.class.getResource("named_data_extend.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon NAMED_DATA_TRUNCATE = new ImageIcon(new ImageIcon(Assets.class.getResource("named_data_truncate.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon FILE_CREATE = new ImageIcon(new ImageIcon(Assets.class.getResource("file_create.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon FILE_DELETE = new ImageIcon(new ImageIcon(Assets.class.getResource("file_delete.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon EA_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("ea_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon SECURITY_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("security_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon RENAME_OLD_NAME = new ImageIcon(new ImageIcon(Assets.class.getResource("rename_old_name.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon RENAME_NEW_NAME = new ImageIcon(new ImageIcon(Assets.class.getResource("rename_new_name.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon INDEXABLE_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("indexable_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon BASIC_INFORMATION_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("basic_information_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon HARD_LINK_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("hard_link_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon COMPRESSION_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("compression_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon ENCRYPTION_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("encryption_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon OBJECT_ID_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("object_id_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon REPARSE_POINT_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("reparse_point_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon STREAM_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("stream_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon TRANSACTED_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("transacted_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon INTEGRITY_CHANGE = new ImageIcon(new ImageIcon(Assets.class.getResource("integrity_change.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon CLOSE = new ImageIcon(new ImageIcon(Assets.class.getResource("close.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
    public static final ImageIcon FILLER = new ImageIcon(new ImageIcon(Assets.class.getResource("filler.png")).getImage().getScaledInstance(UsnUtil.TIMELINE_ENTRY_ICON_WIDTH, -1, Image.SCALE_DEFAULT));
}
