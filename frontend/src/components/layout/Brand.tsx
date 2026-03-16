import { Link } from "react-router-dom"
import logo from "@/assets/logo.png"

export function Brand() {
	return (
		<Link to="/" className="flex items-center space-x-2">
			<img src={logo} alt="Logo" className="h-14 w-14" />
			<span className="font-semibold text-lg">Vizor</span>
		</Link>
	)
}


