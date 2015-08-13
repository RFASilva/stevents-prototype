package core.shared;

public enum Zoom {

	Level_00(0, 256L, 256L), Level_01(1, 512L, 512L), Level_02(2, 1024L, 1024L), Level_03(
			3, 2048L, 2048L), Level_04(4, 4096L, 4096L), Level_05(5, 8192L,
			8192L), Level_06(6, 16384L, 16384L), Level_07(7, 32768L, 32768L), Level_08(
			8, 65536L, 65536L), Level_09(9, 131072L, 131072L), Level_10(10,
			262144L, 262144L), Level_11(11, 524288L, 524288L), Level_12(12,
			1048576L, 1048576L), Level_13(13, 2097152L, 2097152L), Level_14(14,
			4194304L, 4194304L), Level_15(15, 8388608L, 8388608L), Level_16(16,
			16777216L, 16777216L), Level_17(17, 33554432L, 33554432L), Level_18(
			18, 67108864L, 67108864L), Level_19(19, 134217728L, 134217728L), Level_20(
			20, 268435456L, 268435456L);

	private static final String[] LEVELS = { "20", "19", "18", "17", "16",
			"15", "14", "13", "12", "11", "10", "09", "08", "07", "06", "05",
			"04", "03", "02", "01", "00", };

	public static Zoom getLevelByGridSize(final Long gridSize) {
		Zoom level = Zoom.valueOf("Level_" + LEVELS[0]);
		for (int i = 1; i < LEVELS.length && level.getGridSize() > gridSize; i++)
			level = Zoom.valueOf("Level_" + LEVELS[i]);
		return level;
	}

	public static Zoom getNextLevel(final Zoom level) {
		for (int i = 0; i < LEVELS.length - 1; i++) {
			Zoom aux = Zoom.valueOf("Level_" + LEVELS[i]);
			if (level.getZoom() == aux.getZoom())
				return Zoom.valueOf("Level_" + LEVELS[i + 1]);
		}
		return null;
	}

	private Long gridSize;

	private Long pixels;

	private Integer zoom;

	private Zoom(final Integer zoom, final Long pixels, final Long gridSize) {
		this.zoom = zoom;
		this.pixels = pixels;
		this.gridSize = gridSize;
	}

	public Long getGridSize() {
		return gridSize;
	}

	public Long getPixels() {
		return pixels;
	}

	public Integer getZoom() {
		return zoom;
	}

}
