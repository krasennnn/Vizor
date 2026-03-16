import { Link, useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { NavigationMenu, NavigationMenuItem, NavigationMenuList } from "@/components/ui/navigation-menu"
import { useAuth } from "@/contexts/AuthContext"
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { User, LogOut, Home, FileText, Briefcase, LogIn, UserPlus, Users } from "lucide-react"

export function NavLinks() {
	const { isAuthenticated, user, logout } = useAuth()
	const location = useLocation()

	const isActive = (path: string) => location.pathname === path

	return (
		<div className="flex items-center gap-2">
			<NavigationMenu>
				<NavigationMenuList className="gap-1">
					<NavigationMenuItem>
						<Button 
							asChild 
							variant={isActive("/") ? "secondary" : "ghost"} 
							size="sm"
							className="gap-2"
						>
							<Link to="/">
								<Home className="h-4 w-4" />
								Home
							</Link>
						</Button>
					</NavigationMenuItem>
					{isAuthenticated && (
						<>
							<NavigationMenuItem>
								<Button 
									asChild 
									variant={isActive("/contracts") ? "secondary" : "ghost"} 
									size="sm"
									className="gap-2"
								>
									<Link to="/contracts">
										<FileText className="h-4 w-4" />
										Contracts
									</Link>
								</Button>
							</NavigationMenuItem>
							<NavigationMenuItem>
								<Button 
									asChild 
									variant={isActive("/campaign") ? "secondary" : "ghost"} 
									size="sm"
									className="gap-2"
								>
									<Link to="/campaign">
										<Briefcase className="h-4 w-4" />
										Campaigns
									</Link>
								</Button>
							</NavigationMenuItem>
							{user?.roles?.includes("CREATOR") && (
								<NavigationMenuItem>
									<Button 
										asChild 
										variant={isActive("/accounts") ? "secondary" : "ghost"} 
										size="sm"
										className="gap-2"
									>
										<Link to="/accounts">
											<Users className="h-4 w-4" />
											Accounts
										</Link>
									</Button>
								</NavigationMenuItem>
							)}
						</>
					)}
				</NavigationMenuList>
			</NavigationMenu>
			{isAuthenticated ? (
				<DropdownMenu>
					<DropdownMenuTrigger asChild>
						<Button variant="ghost" size="icon" className="relative h-9 w-9 rounded-full">
							<div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10">
								<User className="h-4 w-4" />
							</div>
							<span className="sr-only">User menu</span>
						</Button>
					</DropdownMenuTrigger>
					<DropdownMenuContent align="end" className="w-56">
						<DropdownMenuLabel>
							<div className="flex flex-col space-y-1">
								<p className="text-sm font-medium leading-none">{user?.username}</p>
								<p className="text-xs leading-none text-muted-foreground">{user?.email}</p>
							</div>
						</DropdownMenuLabel>
						<DropdownMenuSeparator />
						<div className="px-2 py-1.5">
							<div className="flex flex-wrap gap-2">
								{user?.roles?.includes("CREATOR") && (
									<span className="inline-flex items-center rounded-md bg-blue-500/10 px-2 py-1 text-xs font-medium text-blue-700 dark:text-blue-400">
										Creator
									</span>
								)}
								{user?.roles?.includes("OWNER") && (
									<span className="inline-flex items-center rounded-md bg-purple-500/10 px-2 py-1 text-xs font-medium text-purple-700 dark:text-purple-400">
										Owner
									</span>
								)}
							</div>
						</div>
						<DropdownMenuSeparator />
						<DropdownMenuItem onClick={logout} className="gap-2 text-red-600 focus:text-red-600">
							<LogOut className="h-4 w-4" />
							Logout
						</DropdownMenuItem>
					</DropdownMenuContent>
				</DropdownMenu>
			) : (
				<div className="flex items-center gap-2">
					<Button asChild variant="ghost" size="sm" className="gap-2">
						<Link to="/login">
							<LogIn className="h-4 w-4" />
							Login
						</Link>
					</Button>
					<Button asChild size="sm" className="gap-2">
						<Link to="/register">
							<UserPlus className="h-4 w-4" />
							Register
						</Link>
					</Button>
				</div>
			)}
		</div>
	)
}
