package automation.utils;

import com.austinv11.servicer.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
@RequiredArgsConstructor
public class DbTablePrinter {

    private final JdbcTemplate jdbcTemplate;

    public String print(String tableName) {
        var tableData = jdbcTemplate.queryForList(String.format("SELECT * FROM %s", tableName));
        return formatAsTable(tableData);
    }

    private String formatAsTable(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "No data found.";
        }

        List<String> columnNames = getColumnNames(data.get(0));
        List<Integer> columnWidths = getColumnWidths(data, columnNames);

        StringBuilder table = new StringBuilder();

        // Build the table header
        table.append("+");
        for (int width : columnWidths) {
            table.append(String.format("%-" + (width + 2) + "s+", "").replace(' ', '-'));
        }
        table.append("\n");

        table.append("|");
        for (int i = 0; i < columnNames.size(); i++) {
            table.append(String.format(" %-" + columnWidths.get(i) + "s |", columnNames.get(i)));
        }
        table.append("\n");

        // Build the separator line
        table.append("+");
        for (int width : columnWidths) {
            table.append(String.format("%-" + (width + 2) + "s+", "").replace(' ', '-'));
        }
        table.append("\n");

        // Build the table rows
        for (Map<String, Object> row : data) {
            table.append("|");
            for (int i = 0; i < columnNames.size(); i++) {
                table.append(String.format(" %-" + columnWidths.get(i) + "s |", row.get(columnNames.get(i))));
            }
            table.append("\n");
        }

        // Build the final separator line
        table.append("+");
        for (int width : columnWidths) {
            table.append(String.format("%-" + (width + 2) + "s+", "").replace(' ', '-'));
        }

        return table.toString();
    }

    private List<String> getColumnNames(Map<String, Object> row) {
        List<String> columnNames = new ArrayList<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            columnNames.add(entry.getKey());
        }
        return columnNames;
    }

    private List<Integer> getColumnWidths(List<Map<String, Object>> data, List<String> columnNames) {
        List<Integer> columnWidths = new ArrayList<>();

        for (String columnName : columnNames) {
            int maxWidth = columnName.length();
            for (Map<String, Object> row : data) {
                Object value = row.get(columnName);
                if (value != null) {
                    int valueLength = value.toString().length();
                    if (valueLength > maxWidth) {
                        maxWidth = valueLength;
                    }
                }
            }
            columnWidths.add(maxWidth);
        }

        return columnWidths;
    }
}
