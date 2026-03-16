import { useState, useMemo } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { DateRangeFilter } from "@/components/filters/DateRangeFilter";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import type { CampaignResponse } from "@/types/Campaign";
import type { CreatorAnalytics, AnalyticsDataPoint } from "@/types/CampaignAnalytics";
import { ChevronUp, ChevronDown } from "lucide-react";

interface CampaignDetailDialogProps {
  campaign: CampaignResponse;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  analytics?: {
    creators: CreatorAnalytics[];
    timeSeries: AnalyticsDataPoint[];
  };
}

type SortField = "views" | "posts" | "totalViews" | "totalPosts" | "creatorName";
type SortDirection = "asc" | "desc";

export function CampaignDetailDialog({
  campaign,
  open,
  onOpenChange,
  analytics,
}: CampaignDetailDialogProps) {
  const [dateRange, setDateRange] = useState<{ startDate?: Date; endDate?: Date }>({});
  const [sortField, setSortField] = useState<SortField>("totalViews");
  const [sortDirection, setSortDirection] = useState<SortDirection>("desc");

  // Mock data if analytics not provided
  const mockCreators: CreatorAnalytics[] = useMemo(
    () =>
      analytics?.creators || [
        {
          creatorId: 1,
          creatorName: "John Doe",
          creatorEmail: "john@example.com",
          totalViews: 125000,
          totalPosts: 45,
          views: 8500,
          posts: 3,
        },
        {
          creatorId: 2,
          creatorName: "Jane Smith",
          creatorEmail: "jane@example.com",
          totalViews: 98000,
          totalPosts: 38,
          views: 7200,
          posts: 2,
        },
        {
          creatorId: 3,
          creatorName: "Bob Johnson",
          creatorEmail: "bob@example.com",
          totalViews: 156000,
          totalPosts: 52,
          views: 10200,
          posts: 4,
        },
      ],
    [analytics]
  );

  // Generate mock time series data if not provided
  const mockTimeSeries: AnalyticsDataPoint[] = useMemo(() => {
    if (analytics?.timeSeries) return analytics.timeSeries;

    const startDate = dateRange.startDate || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const endDate = dateRange.endDate || new Date();
    const days = Math.ceil((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000));

    return Array.from({ length: Math.min(days, 30) }, (_, i) => {
      const date = new Date(startDate);
      date.setDate(date.getDate() + i);
      return {
        date: date.toISOString().split("T")[0],
        views: Math.floor(Math.random() * 5000) + 5000,
        posts: Math.floor(Math.random() * 5) + 1,
      };
    });
  }, [analytics, dateRange]);

  // Filter time series by date range
  const filteredTimeSeries = useMemo(() => {
    if (!dateRange.startDate && !dateRange.endDate) return mockTimeSeries;

    return mockTimeSeries.filter((point) => {
      const pointDate = new Date(point.date);
      if (dateRange.startDate && pointDate < dateRange.startDate) return false;
      if (dateRange.endDate && pointDate > dateRange.endDate) return false;
      return true;
    });
  }, [mockTimeSeries, dateRange]);

  // Sort creators
  const sortedCreators = useMemo(() => {
    const sorted = [...mockCreators].sort((a, b) => {
      let aVal: number | string;
      let bVal: number | string;

      switch (sortField) {
        case "views":
          aVal = a.views;
          bVal = b.views;
          break;
        case "posts":
          aVal = a.posts;
          bVal = b.posts;
          break;
        case "totalViews":
          aVal = a.totalViews;
          bVal = b.totalViews;
          break;
        case "totalPosts":
          aVal = a.totalPosts;
          bVal = b.totalPosts;
          break;
        case "creatorName":
          aVal = a.creatorName;
          bVal = b.creatorName;
          break;
        default:
          return 0;
      }

      if (typeof aVal === "string" && typeof bVal === "string") {
        return sortDirection === "asc"
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal);
      }

      return sortDirection === "asc" ? (aVal as number) - (bVal as number) : (bVal as number) - (aVal as number);
    });

    return sorted;
  }, [mockCreators, sortField, sortDirection]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("desc");
    }
  };

  const formatNumber = (num: number) => {
    if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(1)}M`;
    if (num >= 1_000) return `${(num / 1_000).toFixed(1)}K`;
    return String(num);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{campaign.name} - Analytics</DialogTitle>
          <DialogDescription>View detailed analytics and creator performance</DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {/* Date Range Filter */}
          <div className="flex items-center gap-4">
            <DateRangeFilter
              startDate={dateRange.startDate}
              endDate={dateRange.endDate}
              onChange={(range) => setDateRange({ startDate: range.startDate, endDate: range.endDate })}
            />
          </div>

          {/* Graph */}
          <div className="border rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">Views & Posts Over Time</h3>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={filteredTimeSeries}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="date"
                  tickFormatter={(value) => new Date(value).toLocaleDateString("en-US", { month: "short", day: "numeric" })}
                />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip
                  labelFormatter={(value) => new Date(value).toLocaleDateString()}
                  formatter={(value: number, name: string) => [
                    name === "views" ? formatNumber(value) : value,
                    name === "views" ? "Views" : "Posts",
                  ]}
                />
                <Legend />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="views"
                  stroke="hsl(var(--chart-1))"
                  strokeWidth={2}
                  name="Views"
                  dot={false}
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="posts"
                  stroke="hsl(var(--chart-2))"
                  strokeWidth={2}
                  name="Posts"
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          {/* Creator Details Table */}
          <div className="border rounded-lg">
            <h3 className="text-lg font-semibold p-4 border-b">Creator Performance</h3>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 -ml-2"
                      onClick={() => handleSort("creatorName")}
                    >
                      Creator
                      {sortField === "creatorName" &&
                        (sortDirection === "asc" ? (
                          <ChevronUp className="ml-1 h-4 w-4" />
                        ) : (
                          <ChevronDown className="ml-1 h-4 w-4" />
                        ))}
                    </Button>
                  </TableHead>
                  <TableHead className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 -mr-2"
                      onClick={() => handleSort("totalViews")}
                    >
                      Total Views
                      {sortField === "totalViews" &&
                        (sortDirection === "asc" ? (
                          <ChevronUp className="ml-1 h-4 w-4" />
                        ) : (
                          <ChevronDown className="ml-1 h-4 w-4" />
                        ))}
                    </Button>
                  </TableHead>
                  <TableHead className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 -mr-2"
                      onClick={() => handleSort("totalPosts")}
                    >
                      Total Posts
                      {sortField === "totalPosts" &&
                        (sortDirection === "asc" ? (
                          <ChevronUp className="ml-1 h-4 w-4" />
                        ) : (
                          <ChevronDown className="ml-1 h-4 w-4" />
                        ))}
                    </Button>
                  </TableHead>
                  <TableHead className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 -mr-2"
                      onClick={() => handleSort("views")}
                    >
                      Period Views
                      {sortField === "views" &&
                        (sortDirection === "asc" ? (
                          <ChevronUp className="ml-1 h-4 w-4" />
                        ) : (
                          <ChevronDown className="ml-1 h-4 w-4" />
                        ))}
                    </Button>
                  </TableHead>
                  <TableHead className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 -mr-2"
                      onClick={() => handleSort("posts")}
                    >
                      Period Posts
                      {sortField === "posts" &&
                        (sortDirection === "asc" ? (
                          <ChevronUp className="ml-1 h-4 w-4" />
                        ) : (
                          <ChevronDown className="ml-1 h-4 w-4" />
                        ))}
                    </Button>
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sortedCreators.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center text-muted-foreground py-6">
                      No creators found
                    </TableCell>
                  </TableRow>
                ) : (
                  sortedCreators.map((creator) => (
                    <TableRow key={creator.creatorId}>
                      <TableCell className="font-medium">
                        <div>
                          <div>{creator.creatorName}</div>
                          <div className="text-sm text-muted-foreground">{creator.creatorEmail}</div>
                        </div>
                      </TableCell>
                      <TableCell className="text-right">{formatNumber(creator.totalViews)}</TableCell>
                      <TableCell className="text-right">{creator.totalPosts}</TableCell>
                      <TableCell className="text-right">{formatNumber(creator.views)}</TableCell>
                      <TableCell className="text-right">{creator.posts}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

