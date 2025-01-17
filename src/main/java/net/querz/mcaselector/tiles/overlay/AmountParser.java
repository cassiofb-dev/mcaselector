package net.querz.mcaselector.tiles.overlay;

public abstract class AmountParser extends OverlayParser {

	public AmountParser(OverlayType type) {
		super(type);
	}

	@Override
	public boolean setMin(String raw) {
		setRawMin(raw);
		if (raw == null || raw.isEmpty()) {
			return setMin((Integer) null);
		}
		try {
			return setMin(Integer.parseInt(raw));
		} catch (NumberFormatException ex) {
			return setMin((Integer) null);
		}
	}

	@Override
	public boolean setMax(String raw) {
		setRawMax(raw);
		if (raw == null || raw.isEmpty()) {
			return setMax((Integer) null);
		}
		try {
			return setMax(Integer.parseInt(raw));
		} catch (NumberFormatException ex) {
			return setMin((Integer) null);
		}
	}
}
