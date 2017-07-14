package com.po.ping.obskoala.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.po.ping.obskoala.datum.IdNameHolder;
import com.po.ping.obskoala.datum.TaskEntry;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UIUtil {
	
	private UIUtil() {}
	
	public static ListCell<IdNameHolder> getComboCellfactory() {
		    return new ListCell<IdNameHolder>() {
		        @Override
		        protected void updateItem(IdNameHolder item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		            } else {
		                setText(item.getName());
		            }
		        }
		    };
	}
	
	public static StringConverter<IdNameHolder> getComboConverter() {
		return new StringConverter<IdNameHolder>() {
		    @Override
		    public String toString(IdNameHolder item) {
		    	return item == null ? null : item.getName();
		    }

		    @Override
		    public IdNameHolder fromString(String personString) {
		        return null; // No conversion fromString needed.
		    }
		};
	}	

	public static Callback<DatePicker, DateCell> datePickerCallBack() {
		return new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(final DatePicker datePicker) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);

						if (item.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata")))) {
							setDisable(true);
						}
					}
				};
			}
		};
	}
	
	public static StringConverter<LocalDate> dateConverter() {
		return new StringConverter<LocalDate>() {
			String pattern = "cccc, dd MMMM yyyy";
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

			@Override
			public String toString(LocalDate date) {
				return date != null ? dateFormatter.format(date) : "";				
			}

			@Override
			public LocalDate fromString(String string) {
				return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
			}
		};
	}
	
	public static <T> void setupCellValueFactory(JFXTreeTableColumn<TaskEntry, T> column, Function<TaskEntry, ObservableValue<T>> mapper) {		

        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<TaskEntry, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

}
