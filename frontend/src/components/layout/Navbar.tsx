import { Brand } from "./Brand"
import { NavLinks } from "./NavLinks"
import { Container } from "@/components/common/Container"

export function Navbar() {
    return(
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 shadow-sm">
        <Container>
          <div className="flex h-16 items-center justify-between">
            {/* Left side: Logo */}
            <Brand />

            {/* Right side: Navigation */}
            <NavLinks />
          </div>
        </Container>
        </header>
    )
}
