package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Status;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlotManager {

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots"));
    }

    public static List<Plot> getPlots(Status... status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT idplot FROM plots WHERE status = ");

        for(int i = 0; i < status.length; i++) {
            query.append("'").append(status[i].name()).append("'");

            query.append((i != status.length - 1) ? " OR status = " : "");
        }

        return listPlots(DatabaseConnection.createStatement().executeQuery(query.toString()));
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "'"));
    }

    public static List<Plot> getPlots(Builder builder, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "' AND status = '" + status.name() + "'"));
    }

    public static List<Plot> getPlots(int cityID, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE idcity = '" + cityID + "' AND status = '" + status.name() + "'"));
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        // Get plot
        while (rs.next()) {
           plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public static Vector CalculatePlotCoordinates(int plotID) {
        // Get row of the plot
        int row = (int) Math.floor((plotID - 1) / getMaxRowsSize()) + 1;

        // Get column of the plot
        int column = (int) ((plotID - 1) % getMaxRowsSize()) + 1;

        int xCoords = getPlotSize() * row;
        int zCoords = getPlotSize() * column;

        return Vector.toBlockPoint(xCoords, 10, zCoords);
    }

    public static int getPlotSize() {
        return 105;
    }

    public static int getMaxRowsSize() {
        return 5;
    }

    public static String getSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("schematic-path");
    }

    public static File getDefaultPlot() {
        return new File(BTEPlotSystem.getPlugin().getConfig().getString("default-plot-schematic"));
    }
}
